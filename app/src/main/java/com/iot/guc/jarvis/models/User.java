package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Shared;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int id;
    private String username, token, type;

    public User(int id, String username, String token, String type) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public void logout(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = Shared.getServer().URL() + "/api/logout";
        Shared.request(context, Request.Method.GET, url, null, true, httpResponse);
    }

    public void changePassword(Context context, String old_password, String new_password, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = Shared.getServer().URL() + "/api/user";
            JSONObject body = new JSONObject();
            body.put("old_password", old_password);
            body.put("new_password", new_password);

            Shared.request(context, Request.Method.PUT, url, body, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void makeAdmin(Context context, String username, final HTTPResponse httpResponse) {
        try {
            String url = Shared.getServer().URL() + "/api/user/updateAuth";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("type", "Admin");

            Shared.request(context, Request.Method.PUT, url, body, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public static void register(Context context, String username, String password, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = Shared.getServer().URL() + "/api/register";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            Shared.request(context, Request.Method.POST, url, body, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public static void login(Context context, String username, String password, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = Shared.getServer().URL() + "/api/login";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            Shared.request(context, Request.Method.POST, url, body, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }
}