package com.kbs.backend.tts;

public interface TtsClient {
    /**
     * @return PCM16LE raw bytes (16kHz mono) 권장. (SoT 고정에 맞춤)
     */
    byte[] synthesizePcm16(String text);
}