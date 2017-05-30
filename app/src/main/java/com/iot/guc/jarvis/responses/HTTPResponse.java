package com.iot.guc.jarvis.responses;

import org.json.JSONObject;

public interface HTTPResponse {
    void onSuccess(int statusCode, JSONObject body);
    void onFailure(int statusCode, JSONObject body);
}
