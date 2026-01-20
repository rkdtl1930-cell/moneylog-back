package com.kbs.backend.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatRouterClient {

    private final RestTemplate restTemplate;

    @Value("${chat.router.base-url}")
    private String baseUrl;

    public String ask(String message, String authHeader) {
        String url = baseUrl + "/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Router로 Authorization 전달 (그대로 전달)
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }

        Map<String, Object> body = Map.of("message", message);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("Chat router returned non-2xx or empty body");
        }

        Object reply = res.getBody().get("reply");
        return reply == null ? "" : reply.toString();
    }
}

