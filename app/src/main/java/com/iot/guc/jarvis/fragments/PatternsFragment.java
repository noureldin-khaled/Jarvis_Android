package com.iot.guc.jarvis.fragments;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.adapters.PatternsAdapter;
import com.iot.guc.jarvis.models.Event;

import java.util.ArrayList;

public class PatternsFragment extends Fragment {

    private ArrayList<ArrayList<Event>> Patterns = new ArrayList<ArrayList<Event>>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patterns,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.Patterns_RecyclerView_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        fillData();
        PatternsAdapter patternsAdapter = new PatternsAdapter(Patterns,getActivity());
        recyclerView.setAdapter(patternsAdapter);

        return view;
    }

    private void fillData() {

        Event ev1 = new Event();
        ev1.setDevice("D1");
        ev1.setTime("12:00");
        ev1.setStatus(false);
        Event ev2 = new Event();
        ev2.setDevice("D2");
        ev2.setTime("12:02");
        ev2.setStatus(true);
        Event ev3 = new Event();
        ev3.setDevice("D3");
        ev3.setTime("01:00");
        ev3.setStatus(false);

        ArrayList<Event> s = new ArrayList<Event>();
        s.add(ev1);
        s.add(ev2);
        s.add(ev3);

        Patterns.add(s);




    }


}
