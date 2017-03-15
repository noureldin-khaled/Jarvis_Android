package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private int id;
    private String name;
    private TYPE type;
    private boolean status;
    private String mac, ip;
    private int room_id;

    public Device(int id, String name, TYPE type, boolean status, String mac, String ip, int room_id) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.mac = mac;
        this.ip = ip;
        this.room_id = room_id;
    }

    public Device(TYPE type, String mac, String ip) {
        this.type = type;
        this.mac = mac;
        this.ip = ip;
    }

    public static void getDevices(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = Shared.getServer().URL() + "/api/device";
        Shared.request(context, Request.Method.GET, url, null, true, httpResponse);
    }

    public static void scanDevices(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = Shared.getServer().URL() + "/api/device/scan";
        Shared.request(context, Request.Method.GET, url, null, true, httpResponse);
    }

    public static void addDevice(Context context, String name, TYPE type, String mac, String ip, int room_id, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = Shared.getServer().URL() + "/api/device";
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("type", type.toString());
            body.put("mac", mac);
            body.put("ip", ip);
            body.put("room_id", room_id);

            Shared.request(context, Request.Method.POST, url, body, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                ", room_id=" + room_id +
                '}';
    }

    public enum TYPE {
        LIGHT_BULB, LOCK;

        @Override
        public String toString() {
            switch (this){
                case LIGHT_BULB: return "Light Bulb";
                case LOCK: return "Lock";
                default:return "";
            }
        }
    }
}
