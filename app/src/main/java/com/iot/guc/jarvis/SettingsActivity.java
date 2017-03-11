package com.iot.guc.jarvis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class SettingsActivity extends AppCompatActivity {
    private TextInputLayout layout_username;
    private EditText username_to_be_made_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView username = (TextView) findViewById(R.id.username);
        TextView type = (TextView) findViewById(R.id.type);
        ImageView type_logo = (ImageView) findViewById(R.id.type_logo);
        LinearLayout make_admin_layout = (LinearLayout) findViewById(R.id.make_admin_layout);
        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        username_to_be_made_admin = (EditText) findViewById(R.id.username_to_be_made_admin);

        User auth = Shared.getAuth();
        username.setText(auth.getUsername());
        type.setText(auth.getType());
        if (auth.getType().equals("Admin")) {
            type_logo.setImageResource(R.drawable.admin);
            make_admin_layout.setVisibility(View.VISIBLE);
        }
        else {
            type_logo.setImageResource(R.drawable.normal);
            make_admin_layout.setVisibility(View.GONE);
        }
    }

    public void changePasswordClicked(View view) {

    }

    public void makeAdminClicked(View view) {
        if (username_to_be_made_admin.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }
        else {
            layout_username.setError(null);
            layout_username.setErrorEnabled(false);
        }

        makeAdmin(username_to_be_made_admin.getText().toString());
    }

    public void makeAdmin(final String username) {
        if (username.isEmpty()) return;

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Shared.getServer().URL() + "/api/user/updateAuth";
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("type", "Admin");
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Making \'" + username + "\' an Admin...");
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    View view = SettingsActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    Toast.makeText(getApplicationContext(), "\'" + username + "\' is now an Admin.", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 500) {
                            new Error().create(SettingsActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        } else if (error.networkResponse.statusCode == 404) {
                            new Error().create(SettingsActivity.this, "\'"+username + "\' doesn\'t exist in the database.", "Invalid Credentials").show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        } else {
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
                                    else {
                                        new Error().create(SettingsActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else {
                                    new Error().create(SettingsActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        new Error().create(SettingsActivity.this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
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
        } catch (JSONException e) {
            new Error().create(this, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_home: {
                super.onBackPressed();
                return true;
            }
            case R.id.action_logout: {
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

                if (!isConnected) {
                    new Error().create(this, "Please Check Your Internet Connection!", "Connection Error").show();
                    return false;
                }

                new Confirmation().create(this, "Are you sure you want to logout?", "Confirmation", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Shared.getAuth().logout(SettingsActivity.this);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
