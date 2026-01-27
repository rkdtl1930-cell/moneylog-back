package com.kbs.backend.websocket.voice;

import lombok.Builder;

@Builder
public record VadConfig(
        int minFrameBytes,      // 40ms 기준: 1280 bytes
        int maxFrameBytes,      // 60ms 기준: 1920 bytes
        int rmsThreshold,       // 에너지 임계값 (기기별 튜닝)
        int minSpeechMs,        // 최소 발화 길이 (너무 짧은 잡음 컷)
        int endSilenceMs        // 이만큼 무음이면 발화 종료
) {
    public static VadConfig defaultConfig() {
        return VadConfig.builder()
                .minFrameBytes(1280)
                .maxFrameBytes(1920)
                .rmsThreshold(900) // 시작값. 필요 시 로그 보고 튜닝
                .minSpeechMs(250)
                .endSilenceMs(600)
                .build();
    }
}
