package com.kbs.backend.controller;

import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.ChatRouterClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatRouterClient chatRouterClient;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // SecurityContext에서 userId 확보
        Long userId = (principal != null) ? principal.getId() : null;

        // 현재 Router 스키마는 message만 받으므로 message만 전달
        String reply = chatRouterClient.ask(req.getMessage());

        return ResponseEntity.ok(new ChatResponse(reply));
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

