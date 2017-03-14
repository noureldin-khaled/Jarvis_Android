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
    private TextInputLayout SettingsActivity_TextInputLayout_UsernameLayout;
    private EditText SettingsActivity_EditText_UsernameToBeMade;
    private ProgressBar SettingsActivity_ProgressBar_Progress;
    private RelativeLayout SettingsActivity_RelativeLayout_MainContentView;
    private RelativeLayout SettingsActivity_RelativeLayout_InformationArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        User auth = Shared.getAuth();
        TextView SettingsActivity_TextView_Username = (TextView) findViewById(R.id.SettingsActivity_TextView_Username);
        SettingsActivity_TextView_Username.setText(auth.getUsername());

        TextView SettingsActivity_TextView_Type = (TextView) findViewById(R.id.SettingsActivity_TextView_Type);
        SettingsActivity_TextView_Type.setText(auth.getType());

        ImageView SettingsActivity_ImageView_TypeLogo = (ImageView) findViewById(R.id.SettingsActivity_ImageView_TypeLogo);
        LinearLayout SettingsActivity_LinearLayout_MakeAdminLayout = (LinearLayout) findViewById(R.id.SettingsActivity_LinearLayout_MakeAdminLayout);
        if (auth.getType().equals("Admin")) {
            SettingsActivity_ImageView_TypeLogo.setImageResource(R.drawable.admin);
            SettingsActivity_LinearLayout_MakeAdminLayout.setVisibility(View.VISIBLE);
        }
        else {
            SettingsActivity_ImageView_TypeLogo.setImageResource(R.drawable.normal);
            SettingsActivity_LinearLayout_MakeAdminLayout.setVisibility(View.GONE);
        }

        SettingsActivity_TextInputLayout_UsernameLayout = (TextInputLayout) findViewById(R.id.SettingsActivity_TextInputLayout_UsernameLayout);
        SettingsActivity_EditText_UsernameToBeMade = (EditText) findViewById(R.id.SettingsActivity_EditText_UsernameToBeMade);
        SettingsActivity_ProgressBar_Progress = (ProgressBar) findViewById(R.id.SettingsActivity_ProgressBar_Progress);
        SettingsActivity_RelativeLayout_MainContentView = (RelativeLayout) findViewById(R.id.SettingsActivity_RelativeLayout_MainContentView);
        SettingsActivity_RelativeLayout_InformationArea = (RelativeLayout) findViewById(R.id.SettingsActivity_RelativeLayout_InformationArea);
    }

    public void changePasswordClicked(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText ChangePasswordDialog_EditText_OldPassword = (EditText) contentView.findViewById(R.id.ChangePasswordDialog_EditText_OldPassword);
        final EditText ChangePasswordDialog_EditText_NewPassword = (EditText) contentView.findViewById(R.id.ChangePasswordDialog_EditText_NewPassword);
        final EditText ChangePasswordDialog_EditText_ConfirmPassword = (EditText) contentView.findViewById(R.id.ChangePasswordDialog_EditText_ConfirmPassword);
        final TextInputLayout ChangePasswordDialog_TextInputLayout_OldPasswordLayout = (TextInputLayout) contentView.findViewById(R.id.ChangePasswordDialog_TextInputLayout_OldPasswordLayout);
        final TextInputLayout ChangePasswordDialog_TextInputLayout_NewPasswordLayout = (TextInputLayout) contentView.findViewById(R.id.ChangePasswordDialog_TextInputLayout_NewPasswordLayout);
        final TextInputLayout ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout = (TextInputLayout) contentView.findViewById(R.id.ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout);
        final ProgressBar ChangePasswordDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.ChangePasswordDialog_ProgressBar_Progress);
        final LinearLayout ChangePasswordDialog_LinearLayout_ChangePasswordForm = (LinearLayout) contentView.findViewById(R.id.ChangePasswordDialog_LinearLayout_ChangePasswordForm);

        new Popup().create(this, contentView, "Save", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog) {
                if (ChangePasswordDialog_EditText_OldPassword.getText().toString().isEmpty()) {
                    ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(true);
                    ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError("Please Enter Your Password");
                }
                else {
                    ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(false);
                    ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError(null);
                }

                if (ChangePasswordDialog_EditText_NewPassword.getText().toString().isEmpty()) {
                    ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setErrorEnabled(true);
                    ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setError("Please Enter a New Password");
                }
                else {
                    ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setErrorEnabled(false);
                    ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setError(null);
                }

                if (ChangePasswordDialog_EditText_ConfirmPassword.getText().toString().isEmpty()) {
                    ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setErrorEnabled(true);
                    ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setError("Please Confirm Your New Password");
                }
                else {
                    if (!ChangePasswordDialog_EditText_NewPassword.getText().toString().equals(ChangePasswordDialog_EditText_ConfirmPassword.getText().toString())) {
                        ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setErrorEnabled(true);
                        ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setError("The Confirm Password has to match the New Password");
                    }
                    else {
                        ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setErrorEnabled(false);
                        ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setError(null);
                    }
                }

                if (!ChangePasswordDialog_EditText_OldPassword.getText().toString().isEmpty() && !ChangePasswordDialog_EditText_NewPassword.getText().toString().isEmpty()
                        && !ChangePasswordDialog_EditText_ConfirmPassword.getText().toString().isEmpty() &&
                        ChangePasswordDialog_EditText_NewPassword.getText().toString().equals(ChangePasswordDialog_EditText_ConfirmPassword.getText().toString())) {
                    ChangePasswordDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    ChangePasswordDialog_LinearLayout_ChangePasswordForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    Shared.getAuth().changePassword(getApplicationContext(), ChangePasswordDialog_EditText_OldPassword.getText().toString()
                            , ChangePasswordDialog_EditText_NewPassword.getText().toString(), new HTTPResponse() {
                                @Override
                                public void onSuccess(int statusCode, JSONObject body) {
                                    Shared.collapseKeyBoard(SettingsActivity.this);
                                    dialog.dismiss();
                                    Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Password Changed Successfully", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int statusCode, JSONObject body) {
                                    ChangePasswordDialog_ProgressBar_Progress.setVisibility(View.GONE);
                                    ChangePasswordDialog_LinearLayout_ChangePasswordForm.setVisibility(View.VISIBLE);

                                    switch (statusCode) {
                                        case Constants.NO_INTERNET_CONNECTION: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case Constants.SERVER_NOT_REACHED: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case 403: {
                                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(true);
                                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError("The Password is incorrect");
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
                                                        ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(true);
                                                        ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError("Please Enter Your Password");
                                                    }
                                                    else {
                                                        dialog.dismiss();
                                                        Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                    }
                                                }

                                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                            } catch (JSONException e) {
                                                dialog.dismiss();
                                                Shared.collapseKeyBoard(SettingsActivity.this);
                                                Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }

                                        }
                                        break;
                                        default: {
                                            dialog.dismiss();
                                            Shared.collapseKeyBoard(SettingsActivity.this);
                                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
        SettingsActivity_TextInputLayout_UsernameLayout.setError(null);
        SettingsActivity_TextInputLayout_UsernameLayout.setErrorEnabled(false);

        if (SettingsActivity_EditText_UsernameToBeMade.getText().toString().isEmpty()) {
            SettingsActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
            SettingsActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
        }

        if (SettingsActivity_EditText_UsernameToBeMade.getText().toString().equals(Shared.getAuth().getUsername())) {
            SettingsActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
            SettingsActivity_TextInputLayout_UsernameLayout.setError("You are already an Admin!");
        }

        if (!SettingsActivity_EditText_UsernameToBeMade.getText().toString().isEmpty() && !SettingsActivity_EditText_UsernameToBeMade.getText().toString().equals(Shared.getAuth().getUsername())) {
            Shared.collapseKeyBoard(SettingsActivity.this);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            Shared.getAuth().makeAdmin(getApplicationContext(), SettingsActivity_EditText_UsernameToBeMade.getText().toString(), new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "\'" + SettingsActivity_EditText_UsernameToBeMade.getText().toString() + "\' is now an Admin", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    switch (statusCode) {
                        case Constants.NO_INTERNET_CONNECTION: {
                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                        }
                        break;
                        case Constants.SERVER_NOT_REACHED: {
                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            makeAdminClicked(view);
                                        }
                                    }).show();
                        }
                        break;
                        case 404: {
                            SettingsActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                            SettingsActivity_TextInputLayout_UsernameLayout.setError("This username doesn\'t exist");
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
                                        SettingsActivity_TextInputLayout_UsernameLayout.setErrorEnabled(true);
                                        SettingsActivity_TextInputLayout_UsernameLayout.setError("Please Enter a Username");
                                    }
                                    else {
                                        Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
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
                                Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
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
                            Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
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
                                                Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE).show();
                                            }
                                            break;
                                            case Constants.SERVER_NOT_REACHED: {
                                                Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                onOptionsItemSelected(item);
                                                            }
                                                        }).show();
                                            }
                                            break;
                                            default: {
                                                Snackbar.make(SettingsActivity_RelativeLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
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
        SettingsActivity_ProgressBar_Progress.setVisibility(show ? View.VISIBLE : View.GONE);
        SettingsActivity_RelativeLayout_InformationArea.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
