package com.iot.guc.jarvis.controllers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.ServerTask;
import com.iot.guc.jarvis.models.Event;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.responses.ServerResponse;
import com.iot.guc.jarvis.ServerTask;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.User;
import com.iot.guc.jarvis.responses.ServerResponse;
import com.iot.guc.jarvis.responses.StringResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText LoginActivity_EditText_Username, LoginActivity_EditText_Password;
    private TextInputLayout LoginActivity_TextInputLayout_UsernameLayout, LoginActivity_TextInputLayout_PasswordLayout;
    private RelativeLayout LoginActivity_RelativeLayout_MainContentView, LoginActivity_RelativeLayout_LoginForm;
    private LinearLayout LoginActivity_LinearLayout_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginActivity_EditText_Username = (EditText) findViewById(R.id.LoginActivity_EditText_Username);
        LoginActivity_EditText_Password = (EditText) findViewById(R.id.LoginActivity_EditText_Password);

        LoginActivity_TextInputLayout_UsernameLayout = (TextInputLayout) findViewById(R.id.LoginActivity_TextInputLayout_UsernameLayout);
        LoginActivity_TextInputLayout_PasswordLayout = (TextInputLayout) findViewById(R.id.LoginActivity_TextInputLayout_PasswordLayout);

        LoginActivity_RelativeLayout_MainContentView = (RelativeLayout) findViewById(R.id.LoginActivity_RelativeLayout_MainContentView);
        LoginActivity_RelativeLayout_LoginForm = (RelativeLayout) findViewById(R.id.LoginActivity_RelativeLayout_LoginForm);

        LoginActivity_LinearLayout_Progress = (LinearLayout) findViewById(R.id.LoginActivity_LinearLayout_Progress);

        init();
    }

    public void showProgress(boolean show) {
        LoginActivity_LinearLayout_Progress.setVisibility(show ? View.VISIBLE : View.GONE);
        LoginActivity_RelativeLayout_LoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void init() {
        showProgress(true);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String auth = sharedPreferences.getString("auth", "");
        if (!auth.isEmpty()) {
            try {
                JSONObject user = new JSONObject(auth);
                Shared.setAuth(new User(user.getInt("id"), user.getString("username"), user.getString("token"), user.getString("type"), user.getString("aes_pu"), user.getString("aes_pr"), user.getString("salt")));
                Shared.setSharedKey(sharedPreferences.getString("sharedKey", ""));
                fetchRooms(false);
            } catch (JSONException e) {
                showProgress(false);
                Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("auth");
                editor.remove("sharedKey");
                editor.commit();
                showProgress(false);
                e.printStackTrace();
            }
        }
        else
            showProgress(false);
    }

    public void register(final String username, final String password, final String salt) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        User.register(getApplicationContext(), username, password, salt, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                preLogin(username, password, salt);
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        register(username, password, salt);
                                    }
                                }).show();
                    }
                    break;
                    case 400: {
                        try {
                            JSONArray arr = body.getJSONArray("errors");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject current = arr.getJSONObject(i);
                                String type = current.getString("msg");
                                String field = current.getString("param");

                                if (type.equals("required")) {
                                    if (field.equals("username")) {
                                        LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                                        LoginActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
                                    }
                                    else if (field.equals("username")) {
                                        LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(true);
                                        LoginActivity_TextInputLayout_PasswordLayout.setError("Please Enter a Password");
                                    }
                                    else {
                                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        register(username, password, salt);
                                                    }
                                                }).show();
                                        break;
                                    }
                                }
                                else if (type.equals("unique violation")) {
                                    if (field.equals("username")) {
                                        LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                                        LoginActivity_TextInputLayout_UsernameLayout.setError("This username is already taken.");
                                    }
                                    else {
                                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        register(username, password, salt);
                                                    }
                                                }).show();
                                        break;
                                    }
                                }
                                else {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    register(username, password, salt);
                                                }
                                            }).show();
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            register(username, password, salt);
                                        }
                                    }).show();
                            e.printStackTrace();
                        }

                    }
                    break;
                    default: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        register(username, password, salt);
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void registerClicked(final View view) {
        LoginActivity_TextInputLayout_UsernameLayout.setError(null);
        LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(false);
        LoginActivity_TextInputLayout_PasswordLayout.setError(null);
        LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(false);

        if (LoginActivity_EditText_Username.getText().toString().isEmpty()) {
            LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
            LoginActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
        }

        if (LoginActivity_EditText_Password.getText().toString().isEmpty()) {
            LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(true);
            LoginActivity_TextInputLayout_PasswordLayout.setError("Please Enter a Password");
        }

        if (LoginActivity_TextInputLayout_UsernameLayout.getError() == null && LoginActivity_TextInputLayout_PasswordLayout.getError() == null) {
            Shared.collapseKeyBoard(LoginActivity.this);
            showProgress(true);

            User.hash(getApplicationContext(), LoginActivity_EditText_Password.getText().toString(), new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    try {
                        String salt = body.getString("salt");
                        String hash = body.getString("hash");

                        register(LoginActivity_EditText_Username.getText().toString(), hash, salt);
                    } catch (JSONException e) {
                        showProgress(false);
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        registerClicked(view);
                                    }
                                }).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    showProgress(false);
                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    registerClicked(view);
                                }
                            }).show();
                }
            });
        }
    }

    public void login(final String username, final String password, final String aes_pu, final String aes_pr, final String salt) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        User.login(getApplicationContext(), username, password, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONObject user = body.getJSONObject("user");
                    user.put("aes_pu", aes_pu);
                    user.put("aes_pr", aes_pr);
                    user.put("salt", salt);
                    Shared.setAuth(new User(user.getInt("id"), user.getString("username"), user.getString("token"), user.getString("type"), aes_pu, aes_pr, salt));
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("auth", user.toString());
                    editor.putString("sharedKey", Shared.getSharedKey());
                    editor.commit();
                    fetchRooms(true);
                } catch (JSONException e) {
                    showProgress(false);
                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    login(username, password, aes_pu, aes_pr, salt);
                                }
                            }).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login(username, password, aes_pu, aes_pr, salt);
                                    }
                                }).show();
                    }
                    break;
                    case 400: {
                        try {
                            JSONArray arr = body.getJSONArray("errors");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject current = arr.getJSONObject(i);
                                String type = current.getString("msg");
                                String field = current.getString("param");

                                if (type.equals("required")) {
                                    if (field.equals("username")) {
                                        LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                                        LoginActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
                                    }
                                    else if (field.equals("password")) {
                                        LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(true);
                                        LoginActivity_TextInputLayout_PasswordLayout.setError("Please Enter a Password");
                                    }
                                    else {
                                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        login(username, password, aes_pu, aes_pr, salt);
                                                    }
                                                }).show();
                                        break;
                                    }
                                }
                                else {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    login(username, password, aes_pu, aes_pr, salt);
                                                }
                                            }).show();
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            login(username, password, aes_pu, aes_pr, salt);
                                        }
                                    }).show();
                            e.printStackTrace();
                        }

                    }
                    break;
                    case 401: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Either The Username or Password is incorrect.", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    default: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login(username, password, aes_pu, aes_pr, salt);
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void hashPassword(final String username, final String password, final String aes_pu, final String aes_pr) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        User.getSalt(getApplicationContext(), username, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    final String salt = body.getString("salt");

                    User.hashPassword(getApplicationContext(), password, salt, new StringResponse() {
                        @Override
                        public void onSuccess(int statusCode, String hash) {
                            login(username, hash, aes_pu, aes_pr, salt);
                        }

                        @Override
                        public void onFailure(int statusCode, String response) {
                            showProgress(false);
                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                default: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    hashPassword(username, password, aes_pu, aes_pr);
                                                }
                                            }).show();
                                }
                            }
                        }
                    });

                } catch (JSONException e) {
                    showProgress(false);
                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    hashPassword(username, password, aes_pu, aes_pr);
                                }
                            }).show();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    default: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        hashPassword(username, password, aes_pu, aes_pr);
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void preLogin(final String username, final String password, final String salt) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        User.getKeys(getApplicationContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    final String aes_pu = body.getString("AesPublicKey");
                    final String aes_pr = body.getString("AesPrivateKey");
                    String sharedKey = body.getString("AesSharedKey");
                    Shared.setSharedKey(sharedKey);
                    User.exchange(getApplicationContext(), username, aes_pu, new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            if (salt != null)
                                login(username, password, aes_pu, aes_pr, salt);
                            else
                                hashPassword(username, password, aes_pu, aes_pr);
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            showProgress(false);
                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    preLogin(username, password, salt);
                                                }
                                            }).show();
                                }
                                break;
                                case 400: {
                                    try {
                                        JSONArray arr = body.getJSONArray("errors");
                                        for (int i = 0; i < arr.length(); i++) {
                                            JSONObject current = arr.getJSONObject(i);
                                            String type = current.getString("msg");
                                            String field = current.getString("param");

                                            if (type.equals("required")) {
                                                if (field.equals("username")) {
                                                    LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                                                    LoginActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
                                                }
                                                else {
                                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                            .setAction("RETRY", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    preLogin(username, password, salt);
                                                                }
                                                            }).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                preLogin(username, password, salt);
                                                            }
                                                        }).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        preLogin(username, password, salt);
                                                    }
                                                }).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                case 404: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Either The Username or Password is incorrect.", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                default: {
                                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    preLogin(username, password, salt);
                                                }
                                            }).show();
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    showProgress(false);
                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    preLogin(username, password, salt);
                                }
                            }).show();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    default: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        preLogin(username, password, salt);
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void loginClicked(final View view) {
        LoginActivity_TextInputLayout_UsernameLayout.setError(null);
        LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(false);
        LoginActivity_TextInputLayout_PasswordLayout.setError(null);
        LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(false);

        if (LoginActivity_EditText_Username.getText().toString().isEmpty()) {
            LoginActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
            LoginActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
        }

        if (LoginActivity_EditText_Password.getText().toString().isEmpty()) {
            LoginActivity_TextInputLayout_PasswordLayout.setErrorEnabled(true);
            LoginActivity_TextInputLayout_PasswordLayout.setError("Please Enter a Password");
        }

        if (LoginActivity_TextInputLayout_UsernameLayout.getError() == null && LoginActivity_TextInputLayout_PasswordLayout.getError() == null) {
            Shared.collapseKeyBoard(LoginActivity.this);
            showProgress(true);

            preLogin(LoginActivity_EditText_Username.getText().toString(), LoginActivity_EditText_Password.getText().toString(), null);
        }
    }

    public void fetchRooms(final boolean fromForm) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        Room.getRooms(getApplicationContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray rooms = body.getJSONArray("rooms");
                    for (int i = 0; i < rooms.length(); i++) {
                        JSONObject current = rooms.getJSONObject(i);
                        Shared.addRoom(new Room(current.getInt("id"), current.getString("name")),getApplicationContext());
                    }

                    fetchDevices(fromForm);
                } catch (JSONException e) {
                    showProgress(false);
                    if (fromForm) {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchRooms(fromForm);
                                    }
                                }).show();
                    }
                    else {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                    }

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        if (fromForm) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchRooms(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    break;
                    default: {
                        if (fromForm) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchRooms(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    public void fetchDevices(final boolean fromForm) {
        Shared.collapseKeyBoard(LoginActivity.this);
        showProgress(true);
        Device.getDevices(getApplicationContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray devices = body.getJSONArray("devices");
                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject current = devices.getJSONObject(i);
                        Shared.addDevice(new Device(current.getInt("id"), current.getString("name"),
                                current.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK,
                                current.getBoolean("status"), current.getString("mac"), current.getString("ip"), current.getInt("room_id")));
                    }

                    fetchPatterns();
                } catch (JSONException e) {
                    showProgress(false);
                    if (fromForm) {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchDevices(fromForm);
                                    }
                                }).show();
                    }
                    else {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                    }

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        if (fromForm) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchDevices(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    break;
                    default: {
                        if (fromForm) {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchDevices(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(LoginActivity_RelativeLayout_MainContentView, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    public void fetchPatterns(){
        Shared.request(this, Request.Method.POST, "/api/patterns", new JSONObject(), Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray patterns = body.getJSONArray("patterns");
                    ArrayList<ArrayList<Event>> p = new ArrayList<ArrayList<Event>>();
                    for (int i =0; i<patterns.length();i++){
                        JSONArray sequence = patterns.getJSONArray(i);
                        ArrayList<Event> s = new ArrayList<Event>();
                        for (int j =0; j<sequence.length();j++){
                            JSONObject event = sequence.getJSONObject(j);
                            Event e = new Event();
                            e.setTime(event.getString("time"));
                            e.setDevice(event.getString("device"));
                            e.setDevice_id(event.getInt("device_id"));
                            e.setStatus(event.getBoolean("status"));
                            s.add(e);
                        }
                        p.add(s);
                    }

                    Shared.setPatterns(p);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } catch (JSONException e) {
                    Snackbar.make(LoginActivity_RelativeLayout_MainContentView,"Something Went Wrong!",Snackbar.LENGTH_SHORT).setAction("RETRY",new  View.OnClickListener(){

                        @Override
                        public void onClick(View view) {
                            fetchPatterns();
                        }
                    }).show();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                Snackbar.make(LoginActivity_RelativeLayout_MainContentView,"Something Went Wrong!",Snackbar.LENGTH_SHORT).setAction("RETRY",new  View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        fetchPatterns();
                    }
                }).show();
            }
        });
    }
}
