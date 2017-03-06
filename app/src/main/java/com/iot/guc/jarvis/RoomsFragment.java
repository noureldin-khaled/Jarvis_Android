package com.iot.guc.jarvis;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RoomsFragment extends Fragment {

    ListAdapter listAdapter;
    ArrayList<String> items;
    boolean rooms;
    TextView label;
    ListView list;
    public RoomsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage, container, false);
        rooms= true;
        fillList(view);
           return view;
    }

    public void fillList(final View rootView){

        if(rooms){

            list =(ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    rooms = false;
                    fillList(rootView);
                }
            });
            label =(TextView) rootView.findViewById(R.id.list_label);

            label.setText("Rooms");
            items = new ArrayList<String>();
            items.add("Bet Room");
            items.add("Dinning Room");
            items.add("Living Room");
            items.add("Kitchen");
            listAdapter = new ListAdapter(getActivity(),items);
            list.setAdapter(listAdapter);

        }
        else {

            list =(ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //TODO: create Device Fragment Here
                    fillList(rootView);
                }
            });
            label =(TextView) rootView.findViewById(R.id.list_label);

            label.setText("Devices");
            items = new ArrayList<String>();
            items.add("Device 1");
            items.add("Device 2");
            items.add("Device 3");
            items.add("Device 4");
            listAdapter = new ListAdapter(getActivity(),items);
            list.setAdapter(listAdapter);
        }
    }

}
