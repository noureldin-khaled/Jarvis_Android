package com.iot.guc.jarvis.controllers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Confirmation;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.Error;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.fragments.ChatFragment;
import com.iot.guc.jarvis.fragments.RoomFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private CoordinatorLayout main_content;
    private ProgressBar progressBar;
    private TabLayout tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        main_content = (CoordinatorLayout) findViewById(R.id.main_content);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        tabs = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_pattern);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_devices);
    }

    @Override
    public void onBackPressed() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_logout: {
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
                                        Snackbar.make(main_content, "No Internet Connection!", Snackbar.LENGTH_INDEFINITE).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(main_content, "Server Can\'t Be Reached!", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        onOptionsItemSelected(item);
                                                    }
                                                }).show();
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(main_content, "Something Went Wrong!", Snackbar.LENGTH_INDEFINITE)
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
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tabs.setVisibility(show ? View.GONE : View.VISIBLE);
        mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChatFragment();
                case 1:
                    return PlaceholderFragment.newInstance(position+1);
                case 2:
                    return new RoomFragment();
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
}
