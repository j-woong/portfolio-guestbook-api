package com.jwoong.guestbook.park;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// /ws/park 주소로 들어오는 웹소켓 연결을 ParkSocketHandler에 맡긴다
@Configuration
@EnableWebSocket
public class ParkSocketConfig implements WebSocketConfigurer {

    private final ParkSocketHandler parkSocketHandler;
    private final String[] allowedOrigins;

    public ParkSocketConfig(ParkSocketHandler parkSocketHandler,
                            @Value("${app.allowed-origins}") String[] allowedOrigins) {
        this.parkSocketHandler = parkSocketHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 방명록 API와 같은 주소 목록만 연결을 허용한다
        registry.addHandler(parkSocketHandler, "/ws/park").setAllowedOrigins(allowedOrigins);
    }
}
