package com.jwoong.guestbook.park;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 공원에 함께 있는 방문자들의 위치를 서로에게 전달한다
// 서버는 위치를 저장하거나 판정하지 않고, 받은 상태를 검증해서 다른 사람들에게 그대로 넘긴다
@Component
public class ParkSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ParkSocketHandler.class);

    static final int MAX_VISITORS = 30;
    // 공원이 가득 찼을 때 쓰는 종료 코드 (4000번대는 애플리케이션이 정해서 쓰는 범위)
    static final CloseStatus PARK_FULL = new CloseStatus(4001, "park is full");

    private static final int MAX_MESSAGE_BYTES = 512;
    private static final long MIN_INTERVAL_MILLIS = 40;   // 한 사람이 1초에 25번까지만 보낼 수 있다
    private static final long STALE_MILLIS = 10_000;      // 이만큼 소식이 없으면 자리에 없는 것으로 본다
    private static final double MAP_LIMIT = 40;
    private static final double JUMP_LIMIT = 6;
    private static final int NICKNAME_MAX = 12;

    // 키: 웹소켓 세션 id
    private final Map<String, Visitor> visitors = new ConcurrentHashMap<>();
    private final JsonMapper jsonMapper;

    public ParkSocketHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (visitors.size() >= MAX_VISITORS) {
            session.close(PARK_FULL);
            return;
        }
        session.setTextMessageSizeLimit(MAX_MESSAGE_BYTES);
        // 여러 스레드가 한 세션에 동시에 보내도 안전하게 감싼다. 2초 넘게 밀리거나 64KB 넘게 쌓이면 끊는다
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(session, 2000, 64 * 1024);
        Visitor visitor = new Visitor(UUID.randomUUID().toString().substring(0, 8), safeSession);
        visitors.put(session.getId(), visitor);

        long now = System.currentTimeMillis();
        List<PlayerState> others = visitors.values().stream()
                .filter(other -> other != visitor && other.state != null && now - other.lastSeenAt < STALE_MILLIS)
                .map(other -> other.state)
                .toList();
        send(visitor, new ParkMessages.Hello(visitor.id, others));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Visitor visitor = visitors.get(session.getId());
        if (visitor == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - visitor.lastSeenAt < MIN_INTERVAL_MILLIS) {
            return; // 너무 자주 보내면 버린다
        }
        PlayerState state = parse(visitor.id, message.getPayload());
        if (state == null) {
            return; // 형식이 맞지 않는 메시지는 무시한다
        }
        visitor.state = state;
        visitor.lastSeenAt = now;
        broadcastExcept(visitor, state);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Visitor visitor = visitors.remove(session.getId());
        if (visitor != null) {
            broadcastExcept(visitor, new ParkMessages.Bye(visitor.id));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        closeQuietly(session);
    }

    int visitorCount() {
        return visitors.size();
    }

    // 클라이언트가 보낸 값은 믿지 않는다. 숫자인지 확인하고 허용 범위 안으로 맞춘다
    private PlayerState parse(String id, String payload) {
        JsonNode node;
        try {
            node = jsonMapper.readTree(payload);
        } catch (RuntimeException e) {
            return null;
        }
        if (node == null || !node.path("x").isNumber() || !node.path("z").isNumber()
                || !node.path("f").isNumber() || !node.path("y").isNumber()) {
            return null;
        }
        double x = clamp(node.path("x").asDouble(), -MAP_LIMIT, MAP_LIMIT);
        double z = clamp(node.path("z").asDouble(), -MAP_LIMIT, MAP_LIMIT);
        double f = clamp(node.path("f").asDouble(), -Math.PI * 2, Math.PI * 2);
        double y = clamp(node.path("y").asDouble(), 0, JUMP_LIMIT);
        return new PlayerState("s", id, round(x), round(z), round(f), round(y), nickname(node.path("n")));
    }

    private String nickname(JsonNode node) {
        String raw = node.isString() ? node.asString() : "";
        // 줄바꿈 같은 제어 문자를 지우고 앞뒤 공백을 정리한다
        String cleaned = raw.replaceAll("\\p{Cntrl}", "").strip();
        if (cleaned.isEmpty()) {
            return "손님";
        }
        return cleaned.length() > NICKNAME_MAX ? cleaned.substring(0, NICKNAME_MAX) : cleaned;
    }

    private void broadcastExcept(Visitor sender, Object message) {
        TextMessage text = new TextMessage(jsonMapper.writeValueAsString(message));
        for (Visitor visitor : visitors.values()) {
            if (visitor != sender) {
                send(visitor, text);
            }
        }
    }

    private void send(Visitor visitor, Object message) {
        send(visitor, new TextMessage(jsonMapper.writeValueAsString(message)));
    }

    private void send(Visitor visitor, TextMessage text) {
        try {
            if (visitor.session.isOpen()) {
                visitor.session.sendMessage(text);
            }
        } catch (Exception e) {
            // 느리거나 끊긴 연결 하나 때문에 다른 사람들에게 보내는 일이 멈추면 안 된다
            log.debug("방문자 {}에게 보내지 못해 연결을 닫습니다", visitor.id);
            closeQuietly(visitor.session);
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception ignored) {
            // 이미 닫힌 연결
        }
    }

    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    // 접속한 방문자 한 명. state와 lastSeenAt은 그 방문자의 메시지를 처리하는 스레드만 바꾼다
    private static final class Visitor {
        private final String id;
        private final WebSocketSession session;
        private volatile PlayerState state;
        private volatile long lastSeenAt;

        private Visitor(String id, WebSocketSession session) {
            this.id = id;
            this.session = session;
        }
    }
}
