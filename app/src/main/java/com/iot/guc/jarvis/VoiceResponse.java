package com.iot.guc.jarvis;

public interface VoiceResponse {
    void onSuccess(String result);
    void onError(int error);
}
