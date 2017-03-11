package com.iot.guc.jarvis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText username_edit, password_edit;
    private TextInputLayout layout_username, layout_password;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String authString = sharedPreferences.getString("auth", "");
        if (!authString.isEmpty()) {
            try {
                JSONObject jsonUser = new JSONObject(authString);
                Shared.setAuth(new User(jsonUser.getInt("id"), jsonUser.getString("username"), jsonUser.getString("token"), jsonUser.getString("type")));
                fetchRooms();
                return;
            } catch (JSONException e) {
                new Error().create(this, "Opss.. An error occured while trying to login automatically.\nPlease try to login manually.", "Opss").show();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        };

        setContentView(R.layout.activity_login);

        username_edit = (EditText) findViewById(R.id.username);
        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        password_edit = (EditText) findViewById(R.id.password);
        layout_password = (TextInputLayout) findViewById(R.id.layout_password);
    }

    public void registerClicked(View view) {
        if (username_edit.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }
        else {
            layout_username.setError(null);
            layout_username.setErrorEnabled(false);
        }

        if (password_edit.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
        }
        else {
            layout_password.setError(null);
            layout_password.setErrorEnabled(false);
        }

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            new Error().create(this, "Please Check Your Internet Connection!", "Connection Error").show();
            return;
        }

        register(username_edit.getText().toString(), password_edit.getText().toString());
    }

    public void register(final String username, final String password) {
        if (username.isEmpty() || password.isEmpty()) return;

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Shared.getServer().URL() + "/api/register";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            progressDialog.setMessage("Registering...");
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    login(username, password);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 500) {
                            new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        }
                        else {
                            String err = new String(error.networkResponse.data, "UTF-8");
                            JSONObject json = new JSONObject(err);
                            JSONArray arr = json.getJSONArray("errors");

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject current = arr.getJSONObject(i);
                                String type = current.getString("msg");
                                String field = current.getString("param");

                                if (type.equals("required")) {
                                    if (field.equals("username")) {
                                        layout_username.setErrorEnabled(true);
                                        layout_username.setError("Please Enter a Username");
                                    }
                                    else if (field.equals("password")) {
                                        layout_password.setErrorEnabled(true);
                                        layout_password.setError("Please Enter a Password");
                                    }
                                    else {
                                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else if (type.equals("unique violation")) {
                                    if (field.equals("username")) {
                                        layout_username.setErrorEnabled(true);
                                        layout_username.setError("Username is taken.");
                                    }
                                    else {
                                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else {
                                    new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });

            queue.add(request);
        } catch(JSONException e) {
            new Error().create(this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    public void loginClicked(View view) {
        if (username_edit.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }
        else {
            layout_username.setError(null);
            layout_username.setErrorEnabled(false);
        }

        if (password_edit.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
        }
        else {
            layout_password.setError(null);
            layout_password.setErrorEnabled(false);
        }

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            new Error().create(this, "Please Check Your Internet Connection!", "Connection Error").show();
            return;
        }

        login(username_edit.getText().toString(), password_edit.getText().toString());
    }

    public void login(final String username, final String password) {
        if (username.isEmpty() || password.isEmpty()) return;

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Shared.getServer().URL() + "/api/login";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            progressDialog.setMessage("Logging in...");
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonUser = response.getJSONObject("user");
                        Shared.setAuth(new User(jsonUser.getInt("id"), jsonUser.getString("username"), jsonUser.getString("token"), jsonUser.getString("type")));

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("auth", jsonUser.toString());
                        editor.commit();

                        fetchRooms();
                    } catch (JSONException e) {
                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 500) {
                            new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        }
                        else if (error.networkResponse.statusCode == 400) {
                            String err = new String(error.networkResponse.data, "UTF-8");
                            JSONObject json = new JSONObject(err);
                            JSONArray arr = json.getJSONArray("errors");

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject current = arr.getJSONObject(i);
                                String type = current.getString("msg");
                                String field = current.getString("param");

                                if (type.equals("required")) {
                                    if (field.equals("username")) {
                                        layout_username.setErrorEnabled(true);
                                        layout_username.setError("Please Enter a Username");
                                    }
                                    else if (field.equals("password")) {
                                        layout_password.setErrorEnabled(true);
                                        layout_password.setError("Please Enter a Password");
                                    }
                                    else {
                                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else {
                                    new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                    break;
                                }
                            }
                        }
                        else {
                            new Error().create(LoginActivity.this, "Either the Username or Password is incorrect", "Invalid Credentials").show();
                        }
                    } catch (Exception e) {
                        new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });

            queue.add(request);
        } catch(JSONException e) {
            new Error().create(this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    public void fetchRooms() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Shared.getServer().URL() + "/api/room";

        progressDialog.setMessage("Fetching Rooms...");
        if (!progressDialog.isShowing())
            progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray rooms = response.getJSONArray("rooms");

                    for (int i = 0; i < rooms.length(); i++) {
                        JSONObject current = rooms.getJSONObject(i);
                        int id = current.getInt("id");
                        String name = current.getString("name");
                        Shared.addRoom(new Room(id, name));
                    }

                    fetchDevices();
                } catch (JSONException e) {
                    new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
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

    public void fetchDevices() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Shared.getServer().URL() + "/api/device";

        progressDialog.setMessage("Fetching Your Devices...");
        if (!progressDialog.isShowing())
            progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray devices = response.getJSONArray("devices");

                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject current = devices.getJSONObject(i);
                        int id = current.getInt("id");
                        String name = current.getString("name");
                        Device.TYPE type = current.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK;
                        boolean status = current.getBoolean("status");
                        String mac = current.getString("mac"), ip = current.getString("ip");
                        int room_id = current.getInt("room_id");

                        Shared.addDevice(new Device(id, name, type, status, mac, ip, room_id));
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    startActivity(intent);
                } catch (JSONException e) {
                    new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Error().create(LoginActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
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
