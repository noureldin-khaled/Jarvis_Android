package com.iot.guc.jarvis;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MainActivity2 extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            ChatFragment chatfragment = new ChatFragment();

            chatfragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, chatfragment).commit();
        }
    }

}

