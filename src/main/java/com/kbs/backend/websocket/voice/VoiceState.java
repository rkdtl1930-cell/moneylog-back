package com.kbs.backend.websocket.voice;

public enum VoiceState {
    IDLE,        // 대기 (아직 발화 시작 전)
    LISTENING,   // 발화 중 (프레임 누적)
    FINALIZING   // 발화 종료 감지 후 스냅샷 확정 중
}
