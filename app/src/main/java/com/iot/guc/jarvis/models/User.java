package com.iot.guc.jarvis.models;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.Error;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.controllers.LoginActivity;
import com.iot.guc.jarvis.Shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Shared.getServer().URL() + "/api/logout";

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
                headers.put("Authorization", token);
                return headers;
            }
        };

        queue.add(request);
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
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/user";
            JSONObject body = new JSONObject();
            body.put("old_password", old_password);
            body.put("new_password", new_password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
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
                    headers.put("Authorization", token);
                    return headers;
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }

    public void makeAdmin(Context context, String username, final HTTPResponse httpResponse) {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/user/updateAuth";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("type", "Admin");

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
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
                    headers.put("Authorization", token);
                    return headers;
                }
            };

            queue.add(request);
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
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/register";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
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
            });

            queue.add(request);
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
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/login";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
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
            });

            queue.add(request);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }
}
