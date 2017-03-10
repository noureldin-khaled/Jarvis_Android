package com.iot.guc.jarvis;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomFragment extends Fragment {

    private RoomAdapter roomAdapter;
    private ExpandableListView expListView;
    private ArrayList<Room> Rooms;
    private ArrayList<Device> Devices;
    private List<String> roomList;
    private TextView roomsLabel;
    private HashMap<String, List<String>> deviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        Button b = (Button) view.findViewById(R.id.add_room);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoom();
            }
        });

        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);
        roomsLabel = (TextView) view.findViewById(R.id.list_label);

        Rooms = Shared.getRooms();
        Devices = Shared.getDevices();
        roomList = new ArrayList<String>();
        deviceList = new HashMap<String, List<String>>();

        roomAdapter = new RoomAdapter(getActivity(), roomList, deviceList);

        for (int i = 0; i < Rooms.size(); i++)
            roomList.add(Rooms.get(i).getName());
        addDataToList();
        //Uncomment For Dummy Data
        //prepareListData();
        expListView.setAdapter(roomAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
                Button b = (Button) view.findViewById(R.id.add_device);

                if(expandableListView.isGroupExpanded(i)){
                    b.setVisibility(View.INVISIBLE);
                    expandableListView.collapseGroup(i);
                }
                else {

                    b.setVisibility(View.VISIBLE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addDevice(i);
                        }
                    });
                    expandableListView.expandGroup(i);
                }

                return true;
            }
        });

        return view;
    }

    private void prepareListData() {
        roomList.add("Bed Room");
        roomList.add("Bathroom");
        roomList.add("Kitchen");

        List<String> kitchen = new ArrayList<String>();
        kitchen.add("Bulb");
        kitchen.add("Gun");
        kitchen.add("Neon");

        List<String> Bedroom = new ArrayList<String>();
        Bedroom.add("Device 1");
        Bedroom.add("Device 2");
        Bedroom.add("Device 3");


        List<String> bathroom = new ArrayList<String>();


        deviceList.put(roomList.get(0), Bedroom); // Header, Child data
        deviceList.put(roomList.get(1), kitchen);
        deviceList.put(roomList.get(2), bathroom);
        roomAdapter.notifyDataSetChanged();

    }

    private void addDataToList() {
        HashMap<Integer,ArrayList<String>> hash = new HashMap<Integer, ArrayList<String>>();

        for (int i = 0; i < Rooms.size(); i++)
            hash.put(Rooms.get(i).getId(), new ArrayList<String>());

        for(Device d : Devices){
            ArrayList<String> list = hash.get(d.getRoom_id());
            list.add(d.getName());
            hash.put(d.getRoom_id(), list);
        }

        //Add devices to list view
        for (int i=0; i < Rooms.size();i++)
            deviceList.put(Rooms.get(i).getName(), hash.get(Rooms.get(i).getId()));

        if(deviceList.size() == 0)
            roomsLabel.setText("No Rooms Available");

        roomAdapter.notifyDataSetChanged();
    }

    public void addRoom(){

    }

    public void addDevice(int roomIndex){

    }
}
