package com.iot.guc.jarvis;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "DBG";
    private EditText username, password;
    private TextInputLayout layout_username, layout_password;
    private Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        password = (EditText) findViewById(R.id.password);
        layout_password = (TextInputLayout) findViewById(R.id.layout_password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
    }

    public void registerClicked(View view) {
        if (username.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
            username.requestFocus();
            return;
        }
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);

        if (password.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
            password.requestFocus();
            return;
        }
        layout_password.setError(null);
        layout_password.setErrorEnabled(false);

        Log.i(TAG, "registerClicked: Now you register");
    }

    public void loginClicked(View view) {
        if (username.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
            username.requestFocus();
            return;
        }
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);

        if (password.getText().toString().isEmpty()) {
            layout_password.setErrorEnabled(true);
            layout_password.setError("Please Enter a Password");
            password.requestFocus();
            return;
        }
        layout_password.setError(null);
        layout_password.setErrorEnabled(false);

        Log.i(TAG, "registerClicked: Now you login");
    }
}
