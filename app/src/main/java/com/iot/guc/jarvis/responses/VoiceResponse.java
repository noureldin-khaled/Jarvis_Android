package com.iot.guc.jarvis.responses;

public interface VoiceResponse {
    void onSuccess(String result);
    void onError(int error);
}
