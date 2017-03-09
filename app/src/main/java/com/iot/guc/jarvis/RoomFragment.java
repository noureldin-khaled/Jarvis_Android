package com.iot.guc.jarvis;


import android.os.Bundle;
import android.support.annotation.IntegerRes;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RoomFragment extends Fragment {

    private final static String IP = Common.getIP(), PORT = Common.getPORT(), USER = "com.iot.guc.jarvis.user";
    RoomAdapter listAdapter;
    ExpandableListView expListView;
    ArrayList<Room> Rooms;
    ArrayList<Device> Devices;
    List<String> roomList;
    TextView roomsLabel;
    HashMap<String, List<String>> deviceList;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_room, container, false);

        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);
        final Button addRoom = (Button) view.findViewById(R.id.button);
        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             addRoom();
            }
        });
        roomsLabel = (TextView) view.findViewById(R.id.list_label);
        user = (User) getActivity().getIntent().getExtras().getSerializable(USER);


        Rooms = new ArrayList<>();
        Devices = new ArrayList<>();
        roomList = new ArrayList<String>();
        deviceList = new HashMap<String, List<String>>();
        getRooms();


        listAdapter = new RoomAdapter(getActivity(), roomList, deviceList);


        //Uncomment For Dummy Data
       // prepareListData();
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
                Button b = (Button) view.findViewById(R.id.addDevice);

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
        listAdapter.notifyDataSetChanged();

    }

    private void addDataToList() {

        HashMap<Integer,ArrayList<String>> hash = new HashMap<Integer, ArrayList<String>>();

        for (int i = 0; i < Rooms.size(); i++)
            hash.put(Rooms.get(i).getId(), new ArrayList<String>());


        for(Device d : Devices){

            ArrayList<String> list = hash.get(d.getRoom_id());
            list.add(d.getName());
            hash.put(d.getRoom_id(),list);

        }



        //Add devices to listview
        for (int i=0; i<Rooms.size();i++)
            deviceList.put(Rooms.get(i).getName(), hash.get(Rooms.get(i).getId()));

        if(deviceList.size()==0){
            roomsLabel.setText("No Rooms Available");
        }


        listAdapter.notifyDataSetChanged();
    }

    public void getRooms(){
        RequestQueue q = Volley.newRequestQueue(getActivity());
        String url = "http://" + IP + ":" + PORT + "/api/room";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray rooms = response.getJSONArray("rooms");
                            String json = rooms.toString();
                            Log.e(getActivity().getLocalClassName(),"ROOMS: "+json);
                            if(rooms.length()==0){
                                roomsLabel.setText("No Rooms Available");
                                return;
                            }
                            for(int i=0; i<rooms.length();i++){
                                Gson gson = new Gson();
                                String room = rooms.getJSONObject(i).toString();
                                Room r = gson.fromJson(room,Room.class);
                                String name = r.getName();
                                roomList.add(name);
                                Rooms.add(r);
                            }
                            getDevices();
                            Log.e(getActivity().getLocalClassName(),"ROOMS Length: "+Rooms.size());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Snackbar.make(expListView,"Could not get Rooms",Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(expListView,"Could not get Rooms",Snackbar.LENGTH_LONG).show();

                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getToken() );
                return headers;
            }
        };
        q.add(jsObjRequest);
    }

    public void getDevices(){
        RequestQueue q = Volley.newRequestQueue(getActivity());
        String url = "http://" + IP + ":" + PORT + "/api/device";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray devices = response.getJSONArray("devices");
                    String json = devices.toString();
                    Log.e(getActivity().getLocalClassName(),"DEVICES: "+json);
                    for(int i=0; i<devices.length();i++){
                        Gson gson = new Gson();
                        String device = devices.getJSONObject(i).toString();
                        Device d = gson.fromJson(device,Device.class);
                        Devices.add(d);

                    }
                    Log.e(getActivity().getLocalClassName(),"DEVICES Length: "+Devices.size());
                    addDataToList();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(expListView,"Could not get Devices",Snackbar.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(expListView,"Could not get Devices",Snackbar.LENGTH_LONG).show();

            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", user.getToken() );
                return headers;
            }
        };
        q.add(jsObjRequest);
    }

    public void addRoom(){

    }

    public void addDevice(int roomIndex){

    }


}
