package com.iot.guc.jarvis.responses;

import org.json.JSONObject;

public interface SecurityResponse {
    void onSuccess(int statusCode, String body);
    void onFailure(int statusCode, String body);
}
