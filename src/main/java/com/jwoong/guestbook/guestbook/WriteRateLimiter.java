package com.jwoong.guestbook.guestbook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 같은 방문자(IP 기준)가 정해진 시간 안에 글을 다시 쓰지 못하게 막는다
// 서버 메모리에만 저장하므로 서버가 한 대일 때만 정확하다
@Component
public class WriteRateLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Long> lastWriteAt = new ConcurrentHashMap<>();
    private final long cooldownMillis;

    public WriteRateLimiter(@Value("${app.write-cooldown-seconds}") long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000;
    }

    // 쓸 수 있으면 지금 시각을 기록하고 true, 아직 대기 시간이면 false
    public boolean tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        // 기록이 너무 많이 쌓이면 대기 시간이 지난 것을 지운다
        if (lastWriteAt.size() > CLEANUP_THRESHOLD) {
            lastWriteAt.entrySet().removeIf(e -> now - e.getValue() >= cooldownMillis);
        }
        boolean[] allowed = {false};
        // compute는 같은 키에 대해 한 번에 한 스레드만 실행되므로, 동시에 두 요청이 와도 하나만 통과한다
        lastWriteAt.compute(clientKey, (key, last) -> {
            if (last == null || now - last >= cooldownMillis) {
                allowed[0] = true;
                return now;
            }
            return last;
        });
        return allowed[0];
    }
}
