package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.Shared;

import org.json.JSONException;
import org.json.JSONObject;

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

        String url = "/api/devices";
        Shared.request(context, Request.Method.POST, url, new JSONObject(), Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
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

        String url = "/api/device/scan";
        Shared.request(context, Request.Method.POST, url, new JSONObject(), Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
    }

    public void handle(Context context, boolean status, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = "/api/device/handle/" + getId();
            JSONObject body = new JSONObject();
            body.put("status", status);
            Shared.request(context, Request.Method.POST, url, body, Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
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
            String url = "/api/device";
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("type", type.toString());
            body.put("mac", mac);
            body.put("ip", ip);
            body.put("room_id", room_id);

            Shared.request(context, Request.Method.POST, url, body, Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void deleteDevice( Context context, HTTPResponse httpResponse){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/api/device/delete/" + getId();
        Shared.request(context, Request.Method.POST, url, new JSONObject(), Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
    }

    public void editDevice(Context context, String name, HTTPResponse httpResponse){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try{
            String url = "/api/device/" + getId();
            JSONObject body = new JSONObject();
            body.put("name",name);
            Shared.request(context, Request.Method.PUT, url, body, Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
        } catch (JSONException e){
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE,null);
            e.printStackTrace();
        }
    }

    public void privilegeDevice(Context context, int userId, HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/api/user/" + userId + "/" + getId();
        Shared.request(context, Request.Method.POST, url, new JSONObject(), Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
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
