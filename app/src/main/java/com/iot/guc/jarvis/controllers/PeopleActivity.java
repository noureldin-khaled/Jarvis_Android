package com.iot.guc.jarvis.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.adapters.PeopleAdapter;
import com.iot.guc.jarvis.models.User;
import com.iot.guc.jarvis.responses.HTTPResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PeopleActivity extends AppCompatActivity {
    private LinearLayout PeopleActivity_LinearLayout_MainContentView;
    private ProgressBar PeopleActivity_ProgressBar_Progress;
    private TextView PeopleActivity_TextView_NoUserFound;
    private ListView PeopleActivity_ListView_People;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        Toolbar PeopleActivity_Toolbar_Toolbar = (Toolbar) findViewById(R.id.PeopleActivity_Toolbar_Toolbar);
        setSupportActionBar(PeopleActivity_Toolbar_Toolbar);

        getSupportActionBar().setTitle("People");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PeopleActivity_LinearLayout_MainContentView = (LinearLayout) findViewById(R.id.PeopleActivity_LinearLayout_MainContentView);
        PeopleActivity_ProgressBar_Progress = (ProgressBar) findViewById(R.id.PeopleActivity_ProgressBar_Progress);
        PeopleActivity_TextView_NoUserFound = (TextView) findViewById(R.id.PeopleActivity_TextView_NoUserFound);
        PeopleActivity_ListView_People = (ListView) findViewById(R.id.PeopleActivity_ListView_People);

        fetchUsers();
    }

    public void fetchUsers() {
        showProgress(true);
        User.getUsers(getApplicationContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray users = body.getJSONArray("users");
                    ArrayList<User> a = new ArrayList<>();
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject current = users.getJSONObject(i);
                        a.add(new User(current.getInt("id"), current.getString("username"), current.getString("type")));
                    }

                    showProgress(false);
                    PeopleActivity_ListView_People.setAdapter(new PeopleAdapter(PeopleActivity.this, a));
                    if (a.isEmpty())
                        PeopleActivity_TextView_NoUserFound.setVisibility(View.VISIBLE);
                    else
                        PeopleActivity_TextView_NoUserFound.setVisibility(View.GONE);
                } catch (JSONException e) {
                    PeopleActivity_ProgressBar_Progress.setVisibility(View.GONE);
                    Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    fetchUsers();
                                }
                            }).show();

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                PeopleActivity_ProgressBar_Progress.setVisibility(View.GONE);
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchUsers();
                                    }
                                }).show();
                    }
                    break;
                    default: {
                        Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchUsers();
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void showProgress(boolean show) {
        PeopleActivity_ProgressBar_Progress.setVisibility(show ? View.VISIBLE : View.GONE);
        PeopleActivity_TextView_NoUserFound.setVisibility(show ? View.GONE : View.VISIBLE);
        PeopleActivity_ListView_People.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void updateAuth(final User user) {
        String type = "Admin";
        String message = "Are you sure you want to make \'" + user.getUsername() + "\' an Admin?";
        if (user.getType().equals("Admin")) {
            type = "Normal";
            message = "Are you sure you don\'t want \'" + user.getUsername() + "\' to be an Admin?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String finalType = type;
        builder.setMessage(message)
                .setTitle("Confirmation")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        Shared.getAuth().updateAuth(getApplicationContext(), user.getId(), finalType, new HTTPResponse() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject body) {
                                String res = "\'" + user.getUsername() + "\' is now an Admin";
                                if (user.getType().equals("Admin"))
                                    res = "\'" + user.getUsername() + "\' is no longer an Admin now";
                                Snackbar.make(PeopleActivity_LinearLayout_MainContentView, res, Snackbar.LENGTH_SHORT).show();
                                fetchUsers();
                            }

                            @Override
                            public void onFailure(int statusCode, JSONObject body) {
                                PeopleActivity_ProgressBar_Progress.setVisibility(View.GONE);
                                PeopleActivity_ListView_People.setVisibility(View.VISIBLE);

                                switch (statusCode) {
                                    case Constants.NO_INTERNET_CONNECTION: {
                                        Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(PeopleActivity_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
