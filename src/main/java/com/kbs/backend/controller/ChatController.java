package com.kbs.backend.controller;

import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.ChatRouterClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatRouterClient chatRouterClient;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody ChatRequest req,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpReq){
        try {
            Long userId = (principal != null) ? principal.getId() : null;
            // 프론트가 보낸 JWT를 그대로 꺼낸다.
            String authHeader = httpReq.getHeader("Authorization"); // "Bearer eyJ..."
            // Router 호출 시 함께 전달한다.
            String reply = chatRouterClient.ask(req.getMessage(), authHeader);
            return ResponseEntity.ok(new ChatResponse(reply));
        } catch (ResourceAccessException e) {
            // Router 타임아웃/연결 실패
            return ResponseEntity.status(504).body(Map.of(
                    "error", "CHAT_ROUTER_TIMEOUT",
                    "message", "챗봇 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요."
            ));
        }
    }

    @Data
    public static class ChatRequest {
        private String message;
    }

    @Data
    public static class ChatResponse {
        private final String reply;
    }
}

