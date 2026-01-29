package com.kbs.backend.websocket.voice;

public record VadDecision(
        boolean isSpeech,
        boolean speechStart,
        boolean speechEnd,
        int rms,
        int rmsEma,
        int noiseFloor,
        int startThreshold,
        int endThreshold
) {}
