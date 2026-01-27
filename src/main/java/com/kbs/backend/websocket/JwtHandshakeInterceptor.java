package com.kbs.backend.websocket;

import com.kbs.backend.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_AUTH = "AUTH";
    public static final String ATTR_USER_ID = "USER_ID";

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletReq)) {
            return false;
        }
        HttpServletRequest httpReq = servletReq.getServletRequest();

        // 1) 기존 SecurityFilterChain(JWT Filter)이 handshake에서도 인증을 완료했다면 그걸 우선 사용
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2) 혹시 handshake에서 SecurityContext가 비어있으면(설정에 따라 발생), 직접 JWT 검증 수행
        if (auth == null || !auth.isAuthenticated()) {
            if (!jwtProvider.isTokenValid(httpReq)) {
                return false;
            }
            auth = jwtProvider.getAuthentication(httpReq);
            if (auth == null) return false;
        }

        attributes.put(ATTR_AUTH, auth);

        // userId를 principal에서 꺼내고 싶으면(프로젝트의 UserPrincipal 구조에 맞춰) 여기서 추출
        // 일단은 AUTH만 저장해도 다음 단계 진행 가능.
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}

