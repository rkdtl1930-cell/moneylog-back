package com.kbs.backend.stt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiSttClient implements SttClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.stt.endpoint:https://api.openai.com/v1/audio/transcriptions}")
    private String endpoint;

    @Value("${openai.stt.model:gpt-4o-mini-transcribe}")
    private String model;

    @Value("${openai.stt.language:ko}")
    private String language;

    @Override
    public String transcribeWav(byte[] wavBytes) {
        WebClient webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector())
                .baseUrl(endpoint.replace("/v1/audio/transcriptions", "")) // base only
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        // multipart/form-data: file, model, language
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(wavBytes) {
            @Override
            public String getFilename() {
                return "audio.wav";
            }
        };

        parts.add("file", fileResource);
        parts.add("model", model);
        parts.add("language", language);

        // API 응답은 보통 {"text":"..."} 형태 (문서 기반)
        Map<?, ?> resp = webClient.post()
                .uri("/v1/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (resp == null || resp.get("text") == null) return "";
        return String.valueOf(resp.get("text"));
    }
}

