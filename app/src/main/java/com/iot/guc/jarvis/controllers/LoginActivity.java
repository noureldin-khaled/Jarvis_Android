package com.iot.guc.jarvis.controllers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText username_edit, password_edit;
    private TextInputLayout layout_username, layout_password;
    private RelativeLayout activity_login, form_login;
    private LinearLayout layout_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username_edit = (EditText) findViewById(R.id.username);
        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        password_edit = (EditText) findViewById(R.id.password);
        layout_password = (TextInputLayout) findViewById(R.id.layout_password);
        activity_login = (RelativeLayout) findViewById(R.id.activity_login);
        form_login = (RelativeLayout) findViewById(R.id.form_login);
        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String auth = sharedPreferences.getString("auth", "");
        if (!auth.isEmpty()) {
            showProgress(true);
            try {
                JSONObject user = new JSONObject(auth);
                Shared.setAuth(new User(user.getInt("id"), user.getString("username"), user.getString("token"), user.getString("type")));
                fetchRooms(false);
            } catch (JSONException e) {
                showProgress(false);
                Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("auth");
                editor.commit();
            }
        }
    }

    public void showProgress(boolean show) {
        layout_progress.setVisibility(show ? View.VISIBLE : View.GONE);
        form_login.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void registerClicked(final View view) {
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);
        layout_password.setError(null);
        layout_password.setErrorEnabled(false);

        if (username_edit.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }

        if (password_edit.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
        }

        if (layout_username.getError() == null && layout_password.getError() == null) {
            Shared.collapseKeyBoard(LoginActivity.this);
            showProgress(true);

            User.register(getApplicationContext(), username_edit.getText().toString(), password_edit.getText().toString(), new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    loginClicked(findViewById(R.id.login));
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    showProgress(false);
                    switch (statusCode) {
                        case Constants.NO_INTERNET_CONNECTION: {
                            Snackbar.make(activity_login, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE).show();
                        }
                        break;
                        case Constants.SERVER_NOT_REACHED: {
                            Snackbar.make(activity_login, "Server Can\'t Be Reached!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    registerClicked(view);
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
                                            layout_username.setErrorEnabled(true);
                                            layout_username.setError("Please Enter a Username");
                                        }
                                        else if (field.equals("password")) {
                                            layout_password.setErrorEnabled(true);
                                            layout_password.setError("Please Enter a Password");
                                        }
                                        else {
                                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    registerClicked(view);
                                                }
                                            }).show();
                                            break;
                                        }
                                    }
                                    else if (type.equals("unique violation")) {
                                        if (field.equals("username")) {
                                            layout_username.setErrorEnabled(true);
                                            layout_username.setError("This username is already taken.");
                                        }
                                        else {
                                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    registerClicked(view);
                                                }
                                            }).show();
                                            break;
                                        }
                                    }
                                    else {
                                        Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                registerClicked(view);
                                            }
                                        }).show();
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        registerClicked(view);
                                    }
                                }).show();
                                e.printStackTrace();
                            }

                        }
                        break;
                        default: {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    registerClicked(view);
                                }
                            }).show();
                        }
                    }
                }
            });
        }
    }

    public void loginClicked(final View view) {
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);
        layout_password.setError(null);
        layout_password.setErrorEnabled(false);

        if (username_edit.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }

        if (password_edit.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
        }

        if (layout_username.getError() == null && layout_password.getError() == null) {
            Shared.collapseKeyBoard(LoginActivity.this);
            showProgress(true);

            User.login(getApplicationContext(), username_edit.getText().toString(), password_edit.getText().toString(), new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    try {
                        JSONObject user = body.getJSONObject("user");
                        Shared.setAuth(new User(user.getInt("id"), user.getString("username"), user.getString("token"), user.getString("type")));
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("auth", user.toString());
                        editor.commit();

                        fetchRooms(true);
                    } catch (JSONException e) {
                        showProgress(false);
                        Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        loginClicked(view);
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
                            Snackbar.make(activity_login, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                        }
                        break;
                        case Constants.SERVER_NOT_REACHED: {
                            Snackbar.make(activity_login, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            loginClicked(view);
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
                                            layout_username.setErrorEnabled(true);
                                            layout_username.setError("Please Enter a Username");
                                        }
                                        else if (field.equals("password")) {
                                            layout_password.setErrorEnabled(true);
                                            layout_password.setError("Please Enter a Password");
                                        }
                                        else {
                                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                    .setAction("RETRY", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            loginClicked(view);
                                                        }
                                                    }).show();
                                            break;
                                        }
                                    }
                                    else {
                                        Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        registerClicked(view);
                                                    }
                                                }).show();
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                loginClicked(view);
                                            }
                                        }).show();
                                e.printStackTrace();
                            }

                        }
                        break;
                        case 401: {
                            Snackbar.make(activity_login, "Either The Username or Password is incorrect.", Snackbar.LENGTH_LONG).show();
                        }
                        break;
                        default: {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            loginClicked(view);
                                        }
                                    }).show();
                        }
                    }
                }
            });
        }
    }

    public void fetchRooms(final boolean fromForm) {
        Room.getRooms(getApplicationContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray rooms = body.getJSONArray("rooms");
                    for (int i = 0; i < rooms.length(); i++) {
                        JSONObject current = rooms.getJSONObject(i);
                        Shared.addRoom(new Room(current.getInt("id"), current.getString("name")));
                    }

                    fetchDevices(fromForm);
                } catch (JSONException e) {
                    showProgress(false);
                    if (fromForm) {
                    Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetchRooms(fromForm);
                            }
                        }).show();
                    }
                    else {
                        Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                    }

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(activity_login, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        if (fromForm) {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchRooms(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    break;
                    default: {
                        if (fromForm) {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchRooms(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    public void fetchDevices(final boolean fromForm) {
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

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } catch (JSONException e) {
                    showProgress(false);
                    if (fromForm) {
                        Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchDevices(fromForm);
                                    }
                                }).show();
                    }
                    else {
                        Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                    }

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                showProgress(false);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(activity_login, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        if (fromForm) {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchDevices(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    break;
                    default: {
                        if (fromForm) {
                            Snackbar.make(activity_login, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchDevices(fromForm);
                                        }
                                    }).show();
                        }
                        else {
                            Snackbar.make(activity_login, "Automatic Login Failed!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }
}
