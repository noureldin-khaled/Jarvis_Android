package com.iot.guc.jarvis;

import org.json.JSONObject;

public interface HTTPResponse {
    void onSuccess(int statusCode, JSONObject body);
    void onFailure(int statusCode, JSONObject body);
}
