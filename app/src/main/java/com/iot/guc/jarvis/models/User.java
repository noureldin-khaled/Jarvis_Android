package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.responses.StringResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int id;
    private String username, token, type, aes_pu, aes_pr;

    public User(int id, String username, String token, String type, String aes_pu, String aes_pr) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.type = type;
        this.aes_pu = aes_pu;
        this.aes_pr = aes_pr;
    }

    public User(int id, String username, String type) {
        this.id = id;
        this.username = username;
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

    public String getAes_pu() {
        return aes_pu;
    }

    public void setAes_pu(String aes_pu) {
        this.aes_pu = aes_pu;
    }

    public String getAes_pr() {
        return aes_pr;
    }

    public void setAes_pr(String aes_pr) {
        this.aes_pr = aes_pr;
    }

    public static void hash(Context context, String password, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/register/" + password;
        Shared.JSONcall(context, url, httpResponse);
    }

    public static void getKeys(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/generateKeys";
        Shared.JSONcall(context, url, httpResponse);
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

        String url = "/api/logout";
        Shared.request(context, Request.Method.GET, url, null, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
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
            String url = "/api/user";
            JSONObject body = new JSONObject();
            body.put("old_password", old_password);
            body.put("new_password", new_password);

            Shared.request(context, Request.Method.PUT, url, body, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void updateAuth(Context context, int id, String type, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = "/api/user/" + id;
            JSONObject body = new JSONObject();
            body.put("type", type);

            Shared.request(context, Request.Method.PUT, url, body, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void makeAdmin(Context context, String username, final HTTPResponse httpResponse) {
        try {
            String url = "/api/user/updateAuth";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("type", "Admin");

            Shared.request(context, Request.Method.PUT, url, body, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public static void exchange(Context context, String username, String aes_pu, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = "/api/exchange";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("aes_public_key", aes_pu);

            Shared.request(context, Request.Method.POST, url, body, Constants.NO_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public static void register(final Context context, final String username, final String hash, final String salt, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }


        try {
            String url = "/api/register";
            final JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", hash);
            body.put("salt", salt);

            Shared.request(context, Request.Method.POST, url, body, Constants.NO_HEADERS, null, Constants.RSA_ENCRYPTION, false, httpResponse);
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
            String url = "/api/login";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            Shared.request(context, Request.Method.POST, url, body, Constants.USERNAME_HEADERS, username, Constants.AES_ENCRYPTION, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public static void getSalt(Context context, String username, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/api/salt/" + username;
        Shared.request(context, Request.Method.GET, url, null, Constants.NO_HEADERS, null, Constants.NO_ENCRYPTION, true, httpResponse);
    }

    public static void hashPassword(Context context, String password, String salt, final StringResponse stringResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            stringResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/passwordHash/" + salt + "/" + password;
        Shared.Stringcall(context, url, stringResponse);
    }

    public static void getUsers(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/api/user";
        Shared.request(context, Request.Method.GET, url, null, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
    }

    public static void getUsers(Context context, int device_id, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = "/api/user/" + device_id;
        Shared.request(context, Request.Method.GET, url, null, Constants.AUTH_HEADERS, null, Constants.NO_ENCRYPTION, false, httpResponse);
    }
}
