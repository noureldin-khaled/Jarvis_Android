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

public class Room {
    private int id;
    private String name;

    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void getRooms(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Shared.getServer().URL() + "/api/room";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                httpResponse.onSuccess(200, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    // The server couldn't be reached
                    httpResponse.onFailure(Constants.SERVER_NOT_REACHED, null);
                }
                else {
                    try {
                        String err = new String(error.networkResponse.data, "UTF-8");
                        JSONObject json = new JSONObject(err);
                        httpResponse.onFailure(error.networkResponse.statusCode, json);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        // The app failed
                        httpResponse.onFailure(Constants.APP_FAILURE, null);
                        e.printStackTrace();
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", Shared.getAuth().getToken());
                return headers;
            }
        };

        queue.add(request);
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

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
