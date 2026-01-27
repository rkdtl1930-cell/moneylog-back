package com.kbs.backend.stt;

public interface SttClient {
    String transcribeWav(byte[] wavBytes);
}