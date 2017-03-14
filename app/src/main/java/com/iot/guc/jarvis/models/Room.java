package com.iot.guc.jarvis.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Shared;

import org.json.JSONException;
import org.json.JSONObject;

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
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION,null);
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
