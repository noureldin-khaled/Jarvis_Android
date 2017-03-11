package com.iot.guc.jarvis;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
    private HashMap<String, List<Device>> deviceList;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        Button b = (Button) view.findViewById(R.id.add_room);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());

                View dialogView = inflater.inflate(R.layout.dialog_add,null);
                final EditText entered_name = (EditText) dialogView.findViewById(R.id.name);
                TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
                title.setText("Enter Room Name");
                final TextInputLayout inputLayout = (TextInputLayout) dialogView.findViewById(R.id.layout_name);
                builder.setView(dialogView);
                builder.setPositiveButton("Add",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Shared.addRoom(entered_name.getText().toString());

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog alertDialog= builder.create();
                alertDialog.show();

            }
        });

        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);
        roomsLabel = (TextView) view.findViewById(R.id.list_label);

        Rooms = Shared.getRooms();
        Devices = Shared.getDevices();
        roomList = new ArrayList<String>();
        deviceList = new HashMap<String, List<Device>>();
        prepareDummyData();
        roomAdapter = new RoomAdapter(getActivity(), roomList, deviceList);

        //Uncomment For Dummy Data
        //for (int i = 0; i < Rooms.size(); i++)
            //roomList.add(Rooms.get(i).getName());
        //addDataToList();

        expListView.setAdapter(roomAdapter);

        return view;
    }

    private void prepareDummyData() {
        ArrayList list = new ArrayList();

        roomList.add("Bathroom");
        roomList.add("Kitchen");
        roomList.add("Bedroom");

        for(int i=0;i<4;i++){
            Device d = new Device(i,"Device"+i, Device.TYPE.LIGHT_BULB,true,"","",1);
            list.add(d);
        }
        deviceList.put("Bathroom",list);
        list = new ArrayList();
        for(int i=4;i<8;i++){
            Device d = new Device(i,"Device"+i, Device.TYPE.LIGHT_BULB,true,"","",2);
            list.add(d);
        }
        deviceList.put("Kitchen",list);
        list = new ArrayList();
        deviceList.put("Bedroom",list);
    }

    private void addDataToList() {
        HashMap<Integer,ArrayList<Device>> hash = new HashMap<Integer, ArrayList<Device>>();

        for (int i = 0; i < Rooms.size(); i++)
            hash.put(Rooms.get(i).getId(), new ArrayList<Device>());

        for(Device d : Devices){
            ArrayList<Device> list = hash.get(d.getRoom_id());
            list.add(d);
            hash.put(d.getRoom_id(), list);
        }

        //Add devices to list view
        for (int i=0; i < Rooms.size();i++)
            deviceList.put(Rooms.get(i).getName(), hash.get(Rooms.get(i).getId()));

        if(deviceList.size() == 0)
            roomsLabel.setText("No Rooms Available");

        roomAdapter.notifyDataSetChanged();
    }



}
