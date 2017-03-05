package com.iot.guc.jarvis;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RoomsFragment extends Fragment {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    public RoomsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage, container, false);
        // get the listview
        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
           return view;
    }
    /*
        * Preparing the list data
        */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Bed Room");
        listDataHeader.add("Bathroom");
        listDataHeader.add("Kitchen");

        // Adding child data
        List<String> kitchen = new ArrayList<String>();
        kitchen.add("Bulb");
        kitchen.add("Gun");
        kitchen.add("Neon");


        List<String> Bedroom = new ArrayList<String>();
        Bedroom.add("Device 1");
        Bedroom.add("Device 2");
        Bedroom.add("Device 3");


        List<String> bathroom = new ArrayList<String>();


        listDataChild.put(listDataHeader.get(0), Bedroom); // Header, Child data
        listDataChild.put(listDataHeader.get(1), kitchen);
        listDataChild.put(listDataHeader.get(2), bathroom);
    }


}
