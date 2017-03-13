package com.iot.guc.jarvis.controllers;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.PopupResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {
    private TextInputLayout layout_username;
    private EditText username_to_be_made_admin;
    private ProgressBar progress;
    private RelativeLayout main_layout;
    private RelativeLayout layout_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        User auth = Shared.getAuth();
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(auth.getUsername());

        TextView type = (TextView) findViewById(R.id.type);
        type.setText(auth.getType());

        ImageView type_logo = (ImageView) findViewById(R.id.type_logo);
        LinearLayout make_admin_layout = (LinearLayout) findViewById(R.id.make_admin_layout);
        if (auth.getType().equals("Admin")) {
            type_logo.setImageResource(R.drawable.admin);
            make_admin_layout.setVisibility(View.VISIBLE);
        }
        else {
            type_logo.setImageResource(R.drawable.normal);
            make_admin_layout.setVisibility(View.GONE);
        }

        layout_username = (TextInputLayout) findViewById(R.id.layout_username);
        username_to_be_made_admin = (EditText) findViewById(R.id.username_to_be_made_admin);
        progress = (ProgressBar) findViewById(R.id.progress);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        layout_info = (RelativeLayout) findViewById(R.id.layout_info);
    }

    public void changePasswordClicked(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText old_password = (EditText) contentView.findViewById(R.id.old_password);
        final EditText new_password = (EditText) contentView.findViewById(R.id.new_password);
        final EditText confirm_new_password = (EditText) contentView.findViewById(R.id.confirm_new_password);
        final TextInputLayout layout_old_password = (TextInputLayout) contentView.findViewById(R.id.layout_old_password);
        final TextInputLayout layout_new_password = (TextInputLayout) contentView.findViewById(R.id.layout_new_password);
        final TextInputLayout layout_confirm_new_password = (TextInputLayout) contentView.findViewById(R.id.layout_confirm_new_password);
        final ProgressBar dialog_progress = (ProgressBar) contentView.findViewById(R.id.progress);
        final LinearLayout change_password_form = (LinearLayout) contentView.findViewById(R.id.change_password_form);

        new Popup().create(this, contentView, "Save", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog) {
                if (old_password.getText().toString().isEmpty()) {
                    layout_old_password.setErrorEnabled(true);
                    layout_old_password.setError("Please Enter Your Password");
                }
                else {
                    layout_old_password.setErrorEnabled(false);
                    layout_old_password.setError(null);
                }

                if (new_password.getText().toString().isEmpty()) {
                    layout_new_password.setErrorEnabled(true);
                    layout_new_password.setError("Please Enter a New Password");
                }
                else {
                    layout_new_password.setErrorEnabled(false);
                    layout_new_password.setError(null);
                }

                if (confirm_new_password.getText().toString().isEmpty()) {
                    layout_confirm_new_password.setErrorEnabled(true);
                    layout_confirm_new_password.setError("Please Confirm Your New Password");
                }
                else {
                    if (!new_password.getText().toString().equals(confirm_new_password.getText().toString())) {
                        layout_confirm_new_password.setErrorEnabled(true);
                        layout_confirm_new_password.setError("The Confirm Password has to match the New Password");
                    }
                    else {
                        layout_confirm_new_password.setErrorEnabled(false);
                        layout_confirm_new_password.setError(null);
                    }
                }

                if (!old_password.getText().toString().isEmpty() && !new_password.getText().toString().isEmpty()
                        && !confirm_new_password.getText().toString().isEmpty() &&
                        new_password.getText().toString().equals(confirm_new_password.getText().toString())) {
                    dialog_progress.setVisibility(View.VISIBLE);
                    change_password_form.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    Shared.getAuth().changePassword(getApplicationContext(), old_password.getText().toString()
                            , new_password.getText().toString(), new HTTPResponse() {
                                @Override
                                public void onSuccess(int statusCode, JSONObject body) {
                                    Shared.collapseKeyBoard(SettingsActivity.this);
                                    dialog.dismiss();
                                    Snackbar.make(main_layout, "Password Changed Successfully", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int statusCode, JSONObject body) {
                                    dialog_progress.setVisibility(View.GONE);
                                    change_password_form.setVisibility(View.VISIBLE);

                                    switch (statusCode) {
                                        case Constants.NO_INTERNET_CONNECTION: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(main_layout, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case Constants.SERVER_NOT_REACHED: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(main_layout, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case 403: {
                                            layout_old_password.setErrorEnabled(true);
                                            layout_old_password.setError("The Password is incorrect");
                                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                        }
                                        break;
                                        case 400: {
                                            try {
                                                JSONArray arr = body.getJSONArray("errors");
                                                for (int i = 0; i < arr.length(); i++) {
                                                    JSONObject current = arr.getJSONObject(i);
                                                    String type = current.getString("msg");
                                                    String field = current.getString("param");

                                                    if (type.equals("required") && field.equals("old_password")) {
                                                        layout_old_password.setErrorEnabled(true);
                                                        layout_old_password.setError("Please Enter Your Password");
                                                    }
                                                    else {
                                                        dialog.dismiss();
                                                        Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                    }
                                                }

                                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                            } catch (JSONException e) {
                                                dialog.dismiss();
                                                Shared.collapseKeyBoard(SettingsActivity.this);
                                                Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }

                                        }
                                        break;
                                        default: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                }
            }

            @Override
            public void onNegative(AlertDialog dialog) {
                Shared.collapseKeyBoard(SettingsActivity.this);
                dialog.dismiss();
            }
        });
    }

    public void makeAdminClicked(final View view) {
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);

        if (username_to_be_made_admin.getText().toString().isEmpty()) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("Please Enter a Username");
        }

        if (username_to_be_made_admin.getText().toString().equals(Shared.getAuth().getUsername())) {
            layout_username.setErrorEnabled(true);
            layout_username.setError("You are already an Admin!");
        }

        if (!username_to_be_made_admin.getText().toString().isEmpty() && !username_to_be_made_admin.getText().toString().equals(Shared.getAuth().getUsername())) {
            Shared.collapseKeyBoard(SettingsActivity.this);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            Shared.getAuth().makeAdmin(getApplicationContext(), username_to_be_made_admin.getText().toString(), new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Snackbar.make(main_layout, "\'" + username_to_be_made_admin.getText().toString() + "\' is now an Admin", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    switch (statusCode) {
                        case Constants.NO_INTERNET_CONNECTION: {
                            Snackbar.make(main_layout, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                        }
                        break;
                        case Constants.SERVER_NOT_REACHED: {
                            Snackbar.make(main_layout, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            makeAdminClicked(view);
                                        }
                                    }).show();
                        }
                        break;
                        case 404: {
                            layout_username.setErrorEnabled(true);
                            layout_username.setError("This username doesn\'t exist");
                        }
                        break;
                        case 400: {
                            try {
                                JSONArray arr = body.getJSONArray("errors");
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject current = arr.getJSONObject(i);
                                    String type = current.getString("msg");
                                    String field = current.getString("param");

                                    if (type.equals("required") && field.equals("username")) {
                                        layout_username.setErrorEnabled(true);
                                        layout_username.setError("Please Enter a Username");
                                    }
                                    else {
                                        Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        makeAdminClicked(view);
                                                    }
                                                }).show();
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                makeAdminClicked(view);
                                            }
                                        }).show();
                                e.printStackTrace();
                            }

                        }
                        break;
                        default: {
                            Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            makeAdminClicked(view);
                                        }
                                    }).show();
                        }
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_home: {
                super.onBackPressed();
                return true;
            }
            case R.id.action_logout: {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to logout?")
                        .setTitle("Confirmation")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProgress(true);
                                Shared.getAuth().logout(getApplicationContext(), new HTTPResponse() {
                                    @Override
                                    public void onSuccess(int statusCode, JSONObject body) {
                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.remove("auth");
                                        editor.commit();
                                        Shared.setAuth(null);
                                        Shared.clearRooms();
                                        Shared.clearDevices();

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onFailure(int statusCode, JSONObject body) {
                                        showProgress(false);
                                        switch (statusCode) {
                                            case Constants.NO_INTERNET_CONNECTION: {
                                                Snackbar.make(main_layout, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE).show();
                                            }
                                            break;
                                            case Constants.SERVER_NOT_REACHED: {
                                                Snackbar.make(main_layout, "Server Can\'t Be Reached!", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                onOptionsItemSelected(item);
                                                            }
                                                        }).show();
                                            }
                                            break;
                                            default: {
                                                Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                onOptionsItemSelected(item);
                                                            }
                                                        }).show();
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        }).create().show();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        layout_info.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
