package com.iot.guc.jarvis;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Event;
import com.iot.guc.jarvis.responses.HTTPResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AlertService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent==null){
            Log.e("SERVICE","Intent was null");
        }
        int count = Shared.getServiceCount();
        Event event = Shared.getServiceEvents().get(count);
        count++;
        Shared.setServiceCount(count);

        try {
            JSONObject body = new JSONObject();
            Log.i("Alert", "onStartCommand: Started Service " );

            body.put("status", event.getStatus());
            Shared.request(getApplicationContext(), Request.Method.POST, "/api/device/handle/" + event.getDevice_id(), body, Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, false, true, new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    Log.v("Service","Device handeled");

                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    Log.e("Service","Could not handle device");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }
}
