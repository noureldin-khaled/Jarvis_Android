package com.iot.guc.jarvis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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

    public void logout(final Activity activity) {
        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());
        String url = Shared.getServer().URL() + "/api/logout";

        final ProgressDialog progressDialog = new ProgressDialog(activity.getApplicationContext());
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("auth");
                editor.commit();
                Shared.setAuth(null);
                Shared.clearRooms();
                Shared.clearDevices();

                Intent intent = new Intent(activity.getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                activity.startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
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
}
