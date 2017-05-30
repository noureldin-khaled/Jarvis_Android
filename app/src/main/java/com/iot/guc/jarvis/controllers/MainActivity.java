package com.iot.guc.jarvis.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.camera2.params.Face;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.fragments.ChatFragment;
import com.iot.guc.jarvis.fragments.ContainerFragment;
import com.iot.guc.jarvis.fragments.PatternsFragment;
import com.iot.guc.jarvis.fragments.RoomFragment;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.User;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.responses.PopupResponse;
import com.iot.guc.jarvis.responses.StringResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager MainActivity_ViewPager_Container;
    private CoordinatorLayout MainActivity_CoordinatorLayout_MainContentView;
    private ProgressBar MainActivity_ProgressBar_Progress;
    private TabLayout MainActivity_TabLayout_Tabs;
    private DrawerLayout NavigationDrawer_DrawerLayout_MainLayout;
    private NavigationView NavigationDrawer_NavigationView_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar MainActivity_Toolbar_Toolbar = (Toolbar) findViewById(R.id.MainActivity_Toolbar_Toolbar);
        setSupportActionBar(MainActivity_Toolbar_Toolbar);

        NavigationDrawer_DrawerLayout_MainLayout = (DrawerLayout) findViewById(R.id.NavigationDrawer_DrawerLayout_MainLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, NavigationDrawer_DrawerLayout_MainLayout,
                MainActivity_Toolbar_Toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        NavigationDrawer_DrawerLayout_MainLayout.setDrawerListener(toggle);
        toggle.syncState();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        MainActivity_CoordinatorLayout_MainContentView = (CoordinatorLayout) findViewById(R.id.MainActivity_CoordinatorLayout_MainContentView);
        MainActivity_ProgressBar_Progress = (ProgressBar) findViewById(R.id.MainActivity_ProgressBar_Progress);
        MainActivity_TabLayout_Tabs = (TabLayout) findViewById(R.id.MainActivity_TabLayout_Tabs);

        MainActivity_ViewPager_Container = (ViewPager) findViewById(R.id.MainActivity_ViewPager_Container);
        MainActivity_ViewPager_Container.setAdapter(mSectionsPagerAdapter);
        MainActivity_ViewPager_Container.setCurrentItem(1);
        getSupportActionBar().setTitle("Chat");

        NavigationDrawer_NavigationView_View = (NavigationView) findViewById(R.id.NavigationDrawer_NavigationView_View);
        NavigationDrawer_NavigationView_View.setNavigationItemSelectedListener(this);

        View header = NavigationDrawer_NavigationView_View.getHeaderView(0);
        Menu menu = NavigationDrawer_NavigationView_View.getMenu();
        MenuItem DrawerMenu_Item_People = menu.findItem(R.id.DrawerMenu_Item_People);
        MenuItem DrawerMenu_Item_Train = menu.findItem(R.id.DrawerMenu_Item_Train);
        TextView NavigationDrawerHeader_TextView_Username = (TextView) header.findViewById(R.id.NavigationDrawerHeader_TextView_Username);
        NavigationDrawerHeader_TextView_Username.setText(Shared.getAuth().getUsername());

        ImageView NavigationDrawerHeader_ImageView_Type = (ImageView) header.findViewById(R.id.NavigationDrawerHeader_ImageView_Type);
        if (Shared.getAuth().getType().equals("Admin")) {
            NavigationDrawerHeader_ImageView_Type.setImageResource(R.drawable.admin);
            DrawerMenu_Item_People.setVisible(true);
            DrawerMenu_Item_Train.setVisible(true);
        }
        else {
            NavigationDrawerHeader_ImageView_Type.setImageResource(R.drawable.normal);
            DrawerMenu_Item_People.setVisible(false);
            DrawerMenu_Item_Train.setVisible(false);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.MainActivity_TabLayout_Tabs);

        tabLayout.setupWithViewPager(MainActivity_ViewPager_Container);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_patterns);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_current_chat);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_devices);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                switch (index){
                    case 0: tab.setIcon(R.drawable.ic_current_patterns); getSupportActionBar().setTitle("Patterns"); return;
                    case 1: tab.setIcon(R.drawable.ic_current_chat); getSupportActionBar().setTitle("Chat"); return;
                    case 2: tab.setIcon(R.drawable.ic_current_devices); getSupportActionBar().setTitle("Devices"); return;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                switch (index){
                    case 0: tab.setIcon(R.drawable.ic_patterns); return;
                    case 1: tab.setIcon(R.drawable.ic_chat); Shared.collapseKeyBoard(MainActivity.this); return;
                    case 2: tab.setIcon(R.drawable.ic_devices); return;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (NavigationDrawer_DrawerLayout_MainLayout.isDrawerOpen(GravityCompat.START)) {
            NavigationDrawer_DrawerLayout_MainLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            View contentView = getLayoutInflater().inflate(R.layout.dialog_add_room, null);
            final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
            final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
            final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
            final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);
            TextView AddRoomDialog_TextView_Title = (TextView) contentView.findViewById(R.id.AddRoomDialog_TextView_Title);
            AddRoomDialog_TextView_Title.setVisibility(View.GONE);

            new Popup().create(MainActivity.this, contentView, "Train", new PopupResponse() {
                @Override
                public void onPositive(final android.support.v7.app.AlertDialog dialog) {
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);

                    if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                        AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                        AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Name");
                    }

                    if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                        AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                        AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                        new TrainingTask().execute(data, AddRoomDialog_EditText_RoomName.getText().toString(), new HTTPResponse() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject body) {
                                collapseKeyboard();
                                dialog.dismiss();
                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Trained Successfully", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int statusCode, JSONObject body) {
                                collapseKeyboard();
                                dialog.dismiss();
                                switch (statusCode) {
                                    case Constants.NO_INTERNET_CONNECTION: {
                                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case 400: {
                                        try {
                                            int code = body.getInt("code");
                                            if (code == 5002)
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No faces detected in the provided image", Snackbar.LENGTH_LONG).show();
                                            else if (code == 5010)
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Too many faces detected in the provided image", Snackbar.LENGTH_LONG).show();
                                            else
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        } catch (JSONException e) {
                                            Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onNegative(android.support.v7.app.AlertDialog dialog) {
                    collapseKeyboard();
                    dialog.dismiss();
                }
            });
        }
    }

    public void changePassword(String old_password, String new_password,
                               final android.support.v7.app.AlertDialog dialog,
                               final ProgressBar ChangePasswordDialog_ProgressBar_Progress,
                               final LinearLayout ChangePasswordDialog_LinearLayout_ChangePasswordForm,
                               final TextInputLayout ChangePasswordDialog_TextInputLayout_OldPasswordLayout) {
        Shared.getAuth().changePassword(getApplicationContext(), old_password, new_password, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                collapseKeyboard();
                dialog.dismiss();
                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Password Changed Successfully", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                ChangePasswordDialog_ProgressBar_Progress.setVisibility(View.GONE);
                ChangePasswordDialog_LinearLayout_ChangePasswordForm.setVisibility(View.VISIBLE);

                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        dialog.dismiss();
                        collapseKeyboard();
                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        dialog.dismiss();
                        collapseKeyboard();
                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
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
                                    Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                    break;
                                }
                            }

                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                        } catch (JSONException e) {
                            dialog.dismiss();
                            collapseKeyboard();
                            Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }
                    break;
                    default: {
                        dialog.dismiss();
                        collapseKeyboard();
                        Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.DrawerMenu_Item_ChangePassword: {
                NavigationDrawer_DrawerLayout_MainLayout.closeDrawer(GravityCompat.START);
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
                    public void onPositive(final android.support.v7.app.AlertDialog dialog) {
                        if (ChangePasswordDialog_EditText_OldPassword.getText().toString().isEmpty()) {
                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(true);
                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError("Please Enter Your Password");
                        } else {
                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setErrorEnabled(false);
                            ChangePasswordDialog_TextInputLayout_OldPasswordLayout.setError(null);
                        }

                        if (ChangePasswordDialog_EditText_NewPassword.getText().toString().isEmpty()) {
                            ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setErrorEnabled(true);
                            ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setError("Please Enter a New Password");
                        } else {
                            ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setErrorEnabled(false);
                            ChangePasswordDialog_TextInputLayout_NewPasswordLayout.setError(null);
                        }

                        if (ChangePasswordDialog_EditText_ConfirmPassword.getText().toString().isEmpty()) {
                            ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setErrorEnabled(true);
                            ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setError("Please Confirm Your New Password");
                        } else {
                            if (!ChangePasswordDialog_EditText_NewPassword.getText().toString().equals(ChangePasswordDialog_EditText_ConfirmPassword.getText().toString())) {
                                ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setErrorEnabled(true);
                                ChangePasswordDialog_TextInputLayout_ConfirmPasswordLayout.setError("The Confirm Password has to match the New Password");
                            } else {
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

                            User.hashPassword(getApplicationContext(), ChangePasswordDialog_EditText_OldPassword.getText().toString(), Shared.getAuth().getSalt(), new StringResponse() {
                                @Override
                                public void onSuccess(int statusCode, final String old_password) {
                                    User.hashPassword(getApplicationContext(), ChangePasswordDialog_EditText_NewPassword.getText().toString(), Shared.getAuth().getSalt(), new StringResponse() {
                                        @Override
                                        public void onSuccess(int statusCode, String new_password) {
                                            changePassword(old_password, new_password, dialog, ChangePasswordDialog_ProgressBar_Progress
                                                    , ChangePasswordDialog_LinearLayout_ChangePasswordForm, ChangePasswordDialog_TextInputLayout_OldPasswordLayout);
                                        }

                                        @Override
                                        public void onFailure(int statusCode, String response) {
                                            showProgress(false);
                                            switch (statusCode) {
                                                case Constants.NO_INTERNET_CONNECTION: {
                                                    Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                                }
                                                break;
                                                default: {
                                                    Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(int statusCode, String response) {
                                    showProgress(false);
                                    switch (statusCode) {
                                        case Constants.NO_INTERNET_CONNECTION: {
                                            Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        default: {
                                            Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onNegative(android.support.v7.app.AlertDialog dialog) {
                        collapseKeyboard();
                        dialog.dismiss();
                    }
                });
            }
            break;
            case R.id.DrawerMenu_Item_People: {
                Intent intent = new Intent(this, PeopleActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.DrawerMenu_Item_Train: {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.SELECT_FILE);
            }
            break;
            case R.id.DrawerMenu_Item_Logout: {
                NavigationDrawer_DrawerLayout_MainLayout.closeDrawer(GravityCompat.START);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                                        editor.remove("sharedKey");
                                        editor.commit();
                                        Shared.setAuth(null);
                                        Shared.setSharedKey(null);
                                        Shared.clearRooms();
                                        Shared.clearDevices();
                                        Shared.setSelectedRoom(-1);

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onFailure(int statusCode, JSONObject body) {
                                        showProgress(false);
                                        switch (statusCode) {
                                            case Constants.NO_INTERNET_CONNECTION: {
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE).show();
                                            }
                                            break;
                                            case Constants.SERVER_NOT_REACHED: {
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                onNavigationItemSelected(item);
                                                            }
                                                        }).show();
                                            }
                                            break;
                                            default: {
                                                Snackbar.make(MainActivity_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                onNavigationItemSelected(item);
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
            }
            break;
        }

        return true;
    }

    public void collapseKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void showProgress(boolean show) {
        MainActivity_ProgressBar_Progress.setVisibility(show ? View.VISIBLE : View.GONE);
        MainActivity_TabLayout_Tabs.setVisibility(show ? View.GONE : View.VISIBLE);
        MainActivity_ViewPager_Container.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPostResume() {
        onBackPressed();
        super.onPostResume();
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PatternsFragment();
                case 1:
                    return new ChatFragment();
                case 2:
                    return new ContainerFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    private class TrainingTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Intent data = (Intent) params[0];
            String subject_id = (String) params[1];
            HTTPResponse httpResponse = (HTTPResponse) params[2];

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                User.train(getApplicationContext(), encoded, subject_id, httpResponse);
            } catch (IOException e) {
                // The app failed
                httpResponse.onFailure(Constants.APP_FAILURE, null);
                e.printStackTrace();
            }

            return null;
        }
    }
}
