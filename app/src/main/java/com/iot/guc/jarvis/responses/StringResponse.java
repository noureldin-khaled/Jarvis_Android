package com.iot.guc.jarvis.responses;

import org.json.JSONObject;

public interface StringResponse {
    void onSuccess(int statusCode, String response);
    void onFailure(int statusCode, String response);
}
