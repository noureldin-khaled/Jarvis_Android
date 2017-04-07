
package com.iot.guc.jarvis.responses;

import com.iot.guc.jarvis.models.Params;

public interface ChatResponse {
    void onSuccess(int statusCode, Params param);
}
