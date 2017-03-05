package com.iot.guc.jarvis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {
    private static String IP = "192.168.1.122", PORT = "8000", USER = "com.iot.guc.jarvis.user";
    private final String TAG = "LoginActivity";
    private EditText username, password;
    private TextInputLayout layout_username, layout_password;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        password = (EditText) findViewById(R.id.password);
        layout_password = (TextInputLayout) findViewById(R.id.layout_password);

        progressDialog = new ProgressDialog(this);
    }

    public void registerClicked(View view) throws JSONException {
        if (username.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
            username.requestFocus();
        }
        else {
            layout_username.setError(null);
            layout_username.setErrorEnabled(false);
        }

        if (password.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
            password.requestFocus();
        }
        else {
            layout_password.setError(null);
            layout_password.setErrorEnabled(false);
        }

        register(username.getText().toString(), password.getText().toString());
    }

    public void register(final String username, final String password) throws JSONException {
        if (username.isEmpty() || password.isEmpty()) return;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + IP + ":" + PORT + "/api/register";
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        progressDialog.setMessage("Registering...");
        if (!progressDialog.isShowing())
            progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response.toString());
                try {
                    login(username, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String err = new String(error.networkResponse.data,"UTF-8");
                    Log.e(TAG, "onErrorResponse: " + err, error);
                    progressDialog.dismiss();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        queue.add(request);
    }

    public void loginClicked(View view) throws JSONException {
        if (username.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
            username.requestFocus();
        }
        else {
            layout_username.setError(null);
            layout_username.setErrorEnabled(false);
        }

        if (password.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
            password.requestFocus();
        }
        else {
            layout_password.setError(null);
            layout_password.setErrorEnabled(false);
        }

        login(username.getText().toString(), password.getText().toString());
    }

    public void login(String username, String password) throws JSONException {
        if (username.isEmpty() || password.isEmpty()) return;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + IP + ":" + PORT + "/api/login";
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        progressDialog.setMessage("Logging in...");
        if (!progressDialog.isShowing())
            progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response.toString());

                try {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    User user = new User(response.getInt("id"), response.getString("username"), response.getString("token"), response.getString("type"));
                    intent.putExtra(USER, user);

                    progressDialog.dismiss();
                    startActivity(intent);
                } catch(JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String err = new String(error.networkResponse.data,"UTF-8");
                    Log.e(TAG, "onErrorResponse: " + err, error);
                    progressDialog.dismiss();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        queue.add(request);
    }
}
