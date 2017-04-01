package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.ParseError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.requests.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
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

        String url = Shared.getServer().URL() + "/api/room";
        Shared.request(context, Request.Method.GET, url, null, true, httpResponse);
    }

    public static void addRoom(Context context, String name, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        // here
        String entityUrl = "https://api.api.ai/v1/entities/9088204c-b4bf-4330-bb41-771b99af06ca/entries?v=20150910";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final String jsonString = "[\n" +
                " {\n" +
                "  \"value\": israa,\n" +
                "  \"synonyms\": [\"i\"]\n"+
                " }\n" +
                "]";
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            CustomJsonRequest jsonArrayRequest = new CustomJsonRequest(Request.Method.POST, entityUrl, jsonArray, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("resp", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error",error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer fe2437e5a86740a78ccdfac19d283494");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };
            requestQueue.add(jsonArrayRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //here


        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = Shared.getServer().URL() + "/api/room";
            JSONObject body = new JSONObject();
            body.put("name", name);

            Shared.request(context, Request.Method.POST, url, body, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void deleteRoom(Context context, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        String url = Shared.getServer().URL() + "/api/room/" + getId();
        Shared.request(context, Request.Method.DELETE, url, null, true, httpResponse);
    }

    public void editRoom( Context context,String name, final HTTPResponse httpResponse){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if(!isConnected){
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION,null);
            return;
        }

        String url = Shared.getServer().URL()+"/api/room/"+getId();
        try{

            JSONObject body = new JSONObject();
            body.put("name",name);
            Shared.request(context,Request.Method.PUT,url,body,true,httpResponse);

        }catch (JSONException e){
            httpResponse.onFailure(Constants.APP_FAILURE,null);
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

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
