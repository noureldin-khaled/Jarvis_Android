package com.iot.guc.jarvis.fragments;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.PopupResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.adapters.DeviceAdapter;
import com.iot.guc.jarvis.adapters.RoomAdapter;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomFragment extends Fragment {
    private RoomAdapter roomAdapter;
    private ExpandableListView RoomFragment_ExpandableListView_Rooms;
    private TextView RoomFragment_TextView_RoomsTitle;
    private LinearLayout RoomFragment_LinearLayout_MainContentView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        Button RoomFragment_Button_AddRoom = (Button) view.findViewById(R.id.RoomFragment_Button_AddRoom);
        RoomFragment_Button_AddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoom();
            }
        });

        RoomFragment_ExpandableListView_Rooms = (ExpandableListView) view.findViewById(R.id.RoomFragment_ExpandableListView_Rooms);
        RoomFragment_TextView_RoomsTitle = (TextView) view.findViewById(R.id.RoomFragment_TextView_RoomsTitle);
        RoomFragment_LinearLayout_MainContentView = (LinearLayout) view.findViewById(R.id.RoomFragment_LinearLayout_MainContentView);
        roomAdapter = new RoomAdapter(getActivity().getApplicationContext(), getActivity(), this);
        refill();
        RoomFragment_ExpandableListView_Rooms.setAdapter(roomAdapter);

        return view;
    }

    public void refill() {
        ArrayList<String> rooms = new ArrayList<>();
        HashMap<Integer, ArrayList<Device>> hm = new HashMap<>();
        for (int i = 0; i < Shared.getRooms().size(); i++) {
            rooms.add(Shared.getRooms().get(i).getName());
            hm.put(Shared.getRooms().get(i).getId(), new ArrayList<Device>());
        }

        HashMap<String, ArrayList<Device>> devices = new HashMap<>();
        for (Device device : Shared.getDevices()) {
            ArrayList<Device> list = hm.get(device.getRoom_id());
            list.add(device);
            hm.put(device.getRoom_id(), list);
        }

        for (int i = 0; i < Shared.getRooms().size(); i++)
            devices.put(Shared.getRooms().get(i).getName(), hm.get(Shared.getRooms().get(i).getId()));

        if (Shared.getRooms().isEmpty())
            RoomFragment_TextView_RoomsTitle.setText("No Rooms Found");
        else
            RoomFragment_TextView_RoomsTitle.setText("Rooms");

        roomAdapter.refresh(rooms, devices);
    }

    public void addRoom() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_add_room, null);
        final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
        final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
        final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
        final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);

        new Popup().create(getActivity(), contentView, "Add", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog) {
                AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
                AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);

                if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Room Name");
                }

                if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    Room.addRoom(getContext(), AddRoomDialog_EditText_RoomName.getText().toString(), new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            Shared.collapseKeyBoard(RoomFragment.this);
                            dialog.dismiss();

                            try {
                                JSONObject jsonRoom = body.getJSONObject("room");
                                Shared.addRoom(new Room(jsonRoom.getInt("id") , jsonRoom.getString("name")));
                                refill();
                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Room Created Successfully", Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            AddRoomDialog_ProgressBar_Progress.setVisibility(View.GONE);
                            AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.VISIBLE);
                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case 400: {
                                    try {
                                        JSONArray arr = body.getJSONArray("errors");
                                        for (int i = 0; i < arr.length(); i++) {
                                            JSONObject current = arr.getJSONObject(i);
                                            String type = current.getString("msg");
                                            String field = current.getString("param");

                                            if (type.equals("required")) {
                                                if (field.equals("name")) {
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Room Name");
                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                }
                                                else {
                                                    Shared.collapseKeyBoard(RoomFragment.this);
                                                    dialog.dismiss();
                                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else if (type.equals("unique violation")) {
                                                if (field.equals("name")) {
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("This name is already taken.");
                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                }
                                                else {
                                                    Shared.collapseKeyBoard(RoomFragment.this);
                                                    dialog.dismiss();
                                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Shared.collapseKeyBoard(RoomFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Shared.collapseKeyBoard(RoomFragment.this);
                                        dialog.dismiss();
                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                default: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onNegative(AlertDialog dialog) {
                Shared.collapseKeyBoard(RoomFragment.this);
                dialog.dismiss();
            }
        });
    }

    public void deleteRoom(final int roomIndex) {
        final Room room = Shared.getRooms().get(roomIndex);
        new AlertDialog.Builder(getActivity()).setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this room?\n(All devices in the room will be deleted as well)")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Deleting...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        room.deleteRoom(getContext(), new HTTPResponse() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject body) {
                                Shared.removeRoom(roomIndex);
                                refill();

                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Room Deleted Successfully", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int statusCode, JSONObject body) {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                switch (statusCode) {
                                    case Constants.NO_INTERNET_CONNECTION: {
                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deleteRoom(roomIndex);
                                                    }
                                                }).show();
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deleteRoom(roomIndex);
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
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    public void editRoom(final int roomIndex){
        final Room room = Shared.getRooms().get(roomIndex);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_add_room, null);
        TextView AddRoomDialog_TextView_Title =(TextView) contentView.findViewById(R.id.AddRoomDialog_TextView_Title);
        AddRoomDialog_TextView_Title.setText("Edit Room");
        final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
        final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
        final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
        final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);

        new Popup().create(getActivity(), contentView, "Save", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog) {
                AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
                AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);

                if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Room Name");
                }

                if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    room.editRoom(getContext(), AddRoomDialog_EditText_RoomName.getText().toString(), new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            Shared.collapseKeyBoard(RoomFragment.this);
                            dialog.dismiss();

                            try {
                                JSONObject jsonRoom = body.getJSONObject("room");
                                Shared.editRoom(roomIndex,new Room(jsonRoom.getInt("id") , jsonRoom.getString("name")));
                                refill();
                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Room Saved Successfully", Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            AddRoomDialog_ProgressBar_Progress.setVisibility(View.GONE);
                            AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.VISIBLE);
                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case 400: {
                                    try {
                                        JSONArray arr = body.getJSONArray("errors");
                                        for (int i = 0; i < arr.length(); i++) {
                                            JSONObject current = arr.getJSONObject(i);
                                            String type = current.getString("msg");
                                            String field = current.getString("param");

                                            if (type.equals("required")) {
                                                if (field.equals("name")) {
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Room Name");
                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                }
                                                else {
                                                    Shared.collapseKeyBoard(RoomFragment.this);
                                                    dialog.dismiss();
                                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else if (type.equals("unique violation")) {
                                                if (field.equals("name")) {
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("This name is already taken.");
                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                }
                                                else {
                                                    Shared.collapseKeyBoard(RoomFragment.this);
                                                    dialog.dismiss();
                                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Shared.collapseKeyBoard(RoomFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Shared.collapseKeyBoard(RoomFragment.this);
                                        dialog.dismiss();
                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                default: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }


            }

            @Override
            public void onNegative(AlertDialog dialog) {
                Shared.collapseKeyBoard(RoomFragment.this);
                dialog.dismiss();
            }
        });
    }

    public void addDevice(int roomIndex) {
        // Call Device.scanDevices

        ArrayList<Device> devices = new ArrayList<>();
        devices.add(new Device(Device.TYPE.LIGHT_BULB, "D0-72-17-D2-2A-C6"));
        devices.add(new Device(Device.TYPE.LIGHT_BULB, "D0-72-17-D2-2A-C7"));
        devices.add(new Device(Device.TYPE.LIGHT_BULB, "D0-72-17-D2-2A-C8"));
        devices.add(new Device(Device.TYPE.LIGHT_BULB, "D0-72-17-D2-2A-C9"));
        devices.add(new Device(Device.TYPE.LIGHT_BULB, "D0-72-17-D2-2A-C1"));


        final DeviceAdapter adapter = new DeviceAdapter(getActivity(), devices);

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_scan_devices, null);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(contentView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        dialog.show();

        ListView AddDeviceDialog_ListView_DeviceList = (ListView) contentView.findViewById(R.id.AddDeviceDialog_ListView_DeviceList);
        TextView AddDeviceDialog_TextView_Title = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_Title);
        AddDeviceDialog_TextView_Title.setText(devices.isEmpty() ? "No Devices Found" : "Devices Found");
        AddDeviceDialog_ListView_DeviceList.setAdapter(adapter);

        AddDeviceDialog_ListView_DeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) adapter.getItem(position);
                Log.i(getActivity().getLocalClassName(), "onItemClick: " + position + " : " + device);
                dialog.dismiss();

                View contentView = inflater.inflate(R.layout.dialog_add_devices, null);
                final TextView AddDeviceDialog_TextView_TitleType = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_TitleType);
                AddDeviceDialog_TextView_TitleType.setText(device.getType().toString());
                final TextView AddDeviceDialog_TextView_TitleMac = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_TitleMac);
                AddDeviceDialog_TextView_TitleMac.setText(device.getMac());

                final TextInputLayout AddDeviceDialog_TextInputLayout_DeviceNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddDeviceDialog_TextInputLayout_DeviceNameLayout);
                final EditText AddDeviceDialog_EditText_DeviceName = (EditText) contentView.findViewById(R.id.AddDeviceDialog_EditText_DeviceName);

                new Popup().create(getActivity(), contentView, "Add", new PopupResponse() {
                    @Override
                    public void onPositive(AlertDialog dialog) {
                        AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(false);
                        AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError(null);

                        if (AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("Please Enter a Device Name");
                        }

                        if (!AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
                            // Store the device
                        }
                    }

                    @Override
                    public void onNegative(AlertDialog dialog) {
                        Shared.collapseKeyBoard(RoomFragment.this);
                        dialog.dismiss();
                    }
                });
            }
        });
    }
}
