package com.kbs.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceWebSocketHandler voiceWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceWebSocketHandler, "/ws/voice-chat")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*"); // 개발 단계. 운영에서는 도메인 화이트리스트로 좁히기.
    }
}