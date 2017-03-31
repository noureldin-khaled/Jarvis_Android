package com.iot.guc.jarvis.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.adapters.RoomAdapter;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.responses.PopupResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomFragment extends Fragment {
    private RoomAdapter adapter;
    private CoordinatorLayout RoomFragment_CoordinatorLayout_MainContentView;
    private ListView RoomFragment_ListView_Rooms;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_room, container, false);

        FloatingActionButton RoomFragment_FloatingActionButton_AddRoom = (FloatingActionButton) view.findViewById(R.id.RoomFragment_FloatingActionButton_AddRoom);
        RoomFragment_FloatingActionButton_AddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoom();
            }
        });

        if (Shared.getAuth().getType().equalsIgnoreCase("Normal"))
            RoomFragment_FloatingActionButton_AddRoom.setVisibility(View.GONE);
        RoomFragment_CoordinatorLayout_MainContentView = (CoordinatorLayout) view.findViewById(R.id.RoomFragment_CoordinatorLayout_MainContentView);
        RoomFragment_ListView_Rooms = (ListView) view.findViewById(R.id.RoomFragment_ListView_Rooms);

        RoomFragment_ListView_Rooms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(250);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View contentView = inflater.inflate(R.layout.dialog_options, null);
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(contentView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();
                dialog.getWindow().setLayout(Shared.dpToPx(240, getContext()), ViewGroup.LayoutParams.WRAP_CONTENT);

                final FloatingActionButton OptionDialog_FloatingActionButton_Edit = (FloatingActionButton) contentView.findViewById(R.id.OptionDialog_FloatingActionButton_Edit);
                final FloatingActionButton OptionDialog_FloatingActionButton_Delete = (FloatingActionButton) contentView.findViewById(R.id.OptionDialog_FloatingActionButton_Delete);

                OptionDialog_FloatingActionButton_Edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        editRoom(position);
                    }
                });

                OptionDialog_FloatingActionButton_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(getActivity().getLocalClassName(), "onClick: delete " + position);
                        dialog.dismiss();
                        deleteRoom(position);
                    }
                });
                return false;
            }
        });

        reload();
        return view;
    }

    public void reload() {
        ArrayList<String> a = new ArrayList<>();
        for (Room r : Shared.getRooms())
            a.add(r.getName());
        adapter = new RoomAdapter(getContext(), a);
        RoomFragment_ListView_Rooms.setAdapter(adapter);
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
                                reload();
                                Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Room Created Successfully", Snackbar.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Shared.collapseKeyBoard(RoomFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Shared.collapseKeyBoard(RoomFragment.this);
                                        dialog.dismiss();
                                        Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                default: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View contentView = inflater.inflate(R.layout.dialog_loading, null);
                        final AlertDialog loading = new AlertDialog.Builder(getContext()).setView(contentView).create();
                        loading.show();

                        room.deleteRoom(getContext(), new HTTPResponse() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject body) {
                                Shared.removeRoom(roomIndex);
                                reload();
                                loading.dismiss();
                                Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Room Deleted Successfully", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int statusCode, JSONObject body) {
                                loading.dismiss();
                                switch (statusCode) {
                                    case Constants.NO_INTERNET_CONNECTION: {
                                        Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deleteRoom(roomIndex);
                                                    }
                                                }).show();
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
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

                    final String new_name = AddRoomDialog_EditText_RoomName.getText().toString();

                    room.editRoom(getContext(), new_name, new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            Shared.collapseKeyBoard(RoomFragment.this);
                            dialog.dismiss();
                            Shared.editRoom(roomIndex,new Room(room.getId(), new_name));
                            reload();
                            Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Room Saved Successfully", Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            AddRoomDialog_ProgressBar_Progress.setVisibility(View.GONE);
                            AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.VISIBLE);
                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Shared.collapseKeyBoard(RoomFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Shared.collapseKeyBoard(RoomFragment.this);
                                        dialog.dismiss();
                                        Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                default: {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(RoomFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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

//    public void addDevice(final int roomIndex, final Device device) {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View contentView = inflater.inflate(R.layout.dialog_add_devices, null);
//        final TextView AddDeviceDialog_TextView_TitleType = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_TitleType);
//        AddDeviceDialog_TextView_TitleType.setText(device.getType().toString());
//        final TextView AddDeviceDialog_TextView_TitleMac = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_TitleMac);
//        AddDeviceDialog_TextView_TitleMac.setText(device.getMac());
//
//        final TextInputLayout AddDeviceDialog_TextInputLayout_DeviceNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddDeviceDialog_TextInputLayout_DeviceNameLayout);
//        final EditText AddDeviceDialog_EditText_DeviceName = (EditText) contentView.findViewById(R.id.AddDeviceDialog_EditText_DeviceName);
//
//        new Popup().create(getActivity(), contentView, "Add", new PopupResponse() {
//            @Override
//            public void onPositive(final AlertDialog dialog) {
//                AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(false);
//                AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError(null);
//
//                if (AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
//                    AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
//                    AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("Please Enter a Device Name");
//                }
//
//                if (!AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
//                    Device.addDevice(getContext(), AddDeviceDialog_EditText_DeviceName.getText().toString(),
//                            device.getType(), device.getMac(), device.getIp(), Shared.getRooms().get(roomIndex).getId(),
//                            new HTTPResponse() {
//                                @Override
//                                public void onSuccess(int statusCode, JSONObject body) {
//                                    Shared.collapseKeyBoard(RoomFragment.this);
//                                    dialog.dismiss();
//
//                                    try {
//                                        JSONObject jsonDevice = body.getJSONObject("device");
//                                        Shared.addDevice(new Device(jsonDevice.getInt("id") , jsonDevice.getString("name"),
//                                                jsonDevice.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK, jsonDevice.getBoolean("status"),
//                                                jsonDevice.getString("mac"), jsonDevice.getString("ip"), jsonDevice.getInt("room_id")));
//                                        refill();
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Device Created Successfully", Snackbar.LENGTH_LONG).show();
//                                    } catch (JSONException e) {
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(int statusCode, JSONObject body) {
//                                    switch (statusCode) {
//                                        case Constants.NO_INTERNET_CONNECTION: {
//                                            Shared.collapseKeyBoard(RoomFragment.this);
//                                            dialog.dismiss();
//                                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
//                                        }
//                                        break;
//                                        case Constants.SERVER_NOT_REACHED: {
//                                            Shared.collapseKeyBoard(RoomFragment.this);
//                                            dialog.dismiss();
//                                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
//                                                    .setAction("RETRY", new View.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(View v) {
//                                                            addDevice(roomIndex, device);
//                                                        }
//                                                    }).show();
//                                        }
//                                        break;
//                                        case 400: {
//                                            try {
//                                                JSONArray arr = body.getJSONArray("errors");
//                                                for (int i = 0; i < arr.length(); i++) {
//                                                    JSONObject current = arr.getJSONObject(i);
//                                                    String type = current.getString("msg");
//                                                    String field = current.getString("param");
//
//                                                    if (type.equals("required")) {
//                                                        if (field.equals("name")) {
//                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
//                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("Please Enter a Device Name");
//                                                        }
//                                                        else {
//                                                            Shared.collapseKeyBoard(RoomFragment.this);
//                                                            dialog.dismiss();
//                                                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                                    .setAction("RETRY", new View.OnClickListener() {
//                                                                        @Override
//                                                                        public void onClick(View v) {
//                                                                            addDevice(roomIndex, device);
//                                                                        }
//                                                                    }).show();
//                                                            break;
//                                                        }
//                                                    }
//                                                    else if (type.equals("unique violation")) {
//                                                        if (field.equals("name")) {
//                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
//                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("This name is already taken.");
//                                                        }
//                                                        else {
//                                                            Shared.collapseKeyBoard(RoomFragment.this);
//                                                            dialog.dismiss();
//                                                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                                    .setAction("RETRY", new View.OnClickListener() {
//                                                                        @Override
//                                                                        public void onClick(View v) {
//                                                                            addDevice(roomIndex, device);
//                                                                        }
//                                                                    }).show();
//                                                            break;
//                                                        }
//                                                    }
//                                                    else {
//                                                        Shared.collapseKeyBoard(RoomFragment.this);
//                                                        dialog.dismiss();
//                                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                                .setAction("RETRY", new View.OnClickListener() {
//                                                                    @Override
//                                                                    public void onClick(View v) {
//                                                                        addDevice(roomIndex, device);
//                                                                    }
//                                                                }).show();
//                                                        break;
//                                                    }
//                                                }
//                                            } catch (JSONException e) {
//                                                Shared.collapseKeyBoard(RoomFragment.this);
//                                                dialog.dismiss();
//                                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                        .setAction("RETRY", new View.OnClickListener() {
//                                                            @Override
//                                                            public void onClick(View v) {
//                                                                addDevice(roomIndex, device);
//                                                            }
//                                                        }).show();
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        break;
//                                        default: {
//                                            Shared.collapseKeyBoard(RoomFragment.this);
//                                            dialog.dismiss();
//                                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                    .setAction("RETRY", new View.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(View v) {
//                                                            addDevice(roomIndex, device);
//                                                        }
//                                                    }).show();
//                                            Log.i(getActivity().getLocalClassName(), "onFailure: " + body.toString());
//                                        }
//                                    }
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onNegative(AlertDialog dialog) {
//                Shared.collapseKeyBoard(RoomFragment.this);
//                dialog.dismiss();
//            }
//        });
//    }
//
//    public void scanDevices(final int roomIndex) {
//        final LayoutInflater inflater = getActivity().getLayoutInflater();
//        final View contentView = inflater.inflate(R.layout.dialog_scan_devices, null);
//        final LinearLayout AddDeviceDialog_LinearLayout_Progress = (LinearLayout) contentView.findViewById(R.id.AddDeviceDialog_LinearLayout_Progress);
//        final LinearLayout AddDeviceDialog_LinearLayout_DevicesLayout = (LinearLayout) contentView.findViewById(R.id.AddDeviceDialog_LinearLayout_DevicesLayout);
//        final ListView AddDeviceDialog_ListView_DeviceList = (ListView) contentView.findViewById(R.id.AddDeviceDialog_ListView_DeviceList);
//        final TextView AddDeviceDialog_TextView_Title = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_Title);
//
//        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(contentView)
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .create();
//
//        dialog.show();
//
//        AddDeviceDialog_LinearLayout_Progress.setVisibility(View.VISIBLE);
//        AddDeviceDialog_LinearLayout_DevicesLayout.setVisibility(View.GONE);
//
//        Device.scanDevices(getContext(), new HTTPResponse() {
//            @Override
//            public void onSuccess(int statusCode, JSONObject body) {
//                AddDeviceDialog_LinearLayout_Progress.setVisibility(View.GONE);
//                AddDeviceDialog_LinearLayout_DevicesLayout.setVisibility(View.VISIBLE);
//
//                try {
//                    JSONArray devicesJson = body.getJSONArray("devices");
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    for (int i = 0; i < devicesJson.length(); i++) {
//                        JSONObject current = devicesJson.getJSONObject(i);
//                        devices.add(new Device(current.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK, current.getString("mac"), current.getString("ip")));
//                    }
//
//                    final DeviceAdapter adapter = new DeviceAdapter(getActivity(), devices);
//                    AddDeviceDialog_TextView_Title.setText(devices.isEmpty() ? "No Devices Found" : "Devices Found");
//                    AddDeviceDialog_ListView_DeviceList.setAdapter(adapter);
//
//                    AddDeviceDialog_ListView_DeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            final Device device = (Device) adapter.getItem(position);
//                            dialog.dismiss();
//                            addDevice(roomIndex, device);
//                        }
//                    });
//                } catch (JSONException e) {
//                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                            .setAction("RETRY", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    scanDevices(roomIndex);
//                                }
//                            }).show();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, JSONObject body) {
//                dialog.dismiss();
//                switch (statusCode) {
//                    case Constants.NO_INTERNET_CONNECTION: {
//                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
//                    }
//                    break;
//                    case Constants.SERVER_NOT_REACHED: {
//                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
//                                .setAction("RETRY", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        scanDevices(roomIndex);
//                                    }
//                                }).show();
//                    }
//                    break;
//                    default: {
//                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                .setAction("RETRY", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        scanDevices(roomIndex);
//                                    }
//                                }).show();
//                    }
//                }
//            }
//        });
//    }
//
//    public void deleteDevice(final Device device){
//
//        new AlertDialog.Builder(getActivity()).setTitle("Confirmation")
//                .setMessage("Are you sure you want to delete this Device?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//                        progressDialog.setMessage("Deleting...");
//                        progressDialog.setCancelable(false);
//                        progressDialog.show();
//
//                        device.deleteDevice(getContext(), new HTTPResponse() {
//                            @Override
//                            public void onSuccess(int statusCode, JSONObject body) {
//                                Shared.removeDevice(device);
//                                refill();
//
//                                if (progressDialog.isShowing())
//                                    progressDialog.dismiss();
//                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Device Deleted Successfully", Snackbar.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onFailure(int statusCode, JSONObject body) {
//                                if (progressDialog.isShowing())
//                                    progressDialog.dismiss();
//                                switch (statusCode) {
//                                    case Constants.NO_INTERNET_CONNECTION: {
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
//                                    }
//                                    break;
//                                    case Constants.SERVER_NOT_REACHED: {
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
//                                                .setAction("RETRY", new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View v) {
//                                                        deleteDevice(device);
//                                                    }
//                                                }).show();
//                                    }
//                                    break;
//                                    default: {
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
//                                                .setAction("RETRY", new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View v) {
//                                                        deleteDevice(device);
//                                                    }
//                                                }).show();
//                                    }
//                                }
//                            }
//                        });
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).create().show();
//
//    }
//
//    public void editDevice(final Device device) {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View contentView = inflater.inflate(R.layout.dialog_add_room, null);
//        TextView AddRoomDialog_TextView_Title =(TextView) contentView.findViewById(R.id.AddRoomDialog_TextView_Title);
//        AddRoomDialog_TextView_Title.setText("Edit Device");
//        final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
//        final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
//        final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
//        final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);
//
//        new Popup().create(getActivity(), contentView, "Save", new PopupResponse() {
//            @Override
//            public void onPositive(final AlertDialog dialog) {
//                AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
//                AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);
//
//                if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
//                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
//                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Device Name");
//                }
//
//                if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
//                    AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
//                    AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
//                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
//
//                    final String new_name = AddRoomDialog_EditText_RoomName.getText().toString();
//
//                    device.editDevice(getContext(), new_name, new HTTPResponse() {
//                        @Override
//                        public void onSuccess(int statusCode, JSONObject body) {
//
//                            Shared.collapseKeyBoard(RoomFragment.this);
//                            dialog.dismiss();
//                            int deviceIndex=0;
//                            for(int i =0; i<Shared.getDevices().size();i++){
//                                if(device.getId()==Shared.getDevices().get(i).getId())
//                                    deviceIndex = i;
//                            }
//                            Shared.editDevice(deviceIndex,new Device(device.getId(),  new_name,device.getType(),device.isStatus(),device.getMac(),device.getIp(),device.getRoom_id()));
//                            refill();
//                            Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Device Saved Successfully", Snackbar.LENGTH_LONG).show();
//
//                        }
//
//                        @Override
//                        public void onFailure(int statusCode, JSONObject body) {
//                            AddRoomDialog_ProgressBar_Progress.setVisibility(View.GONE);
//                            AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.VISIBLE);
//                            switch (statusCode) {
//                                case Constants.NO_INTERNET_CONNECTION: {
//                                    Shared.collapseKeyBoard(RoomFragment.this);
//                                    dialog.dismiss();
//                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
//                                }
//                                break;
//                                case Constants.SERVER_NOT_REACHED: {
//                                    Shared.collapseKeyBoard(RoomFragment.this);
//                                    dialog.dismiss();
//                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
//                                }
//                                break;
//                                case 400: {
//                                    try {
//                                        JSONArray arr = body.getJSONArray("errors");
//                                        for (int i = 0; i < arr.length(); i++) {
//                                            JSONObject current = arr.getJSONObject(i);
//                                            String type = current.getString("msg");
//                                            String field = current.getString("param");
//
//                                            if (type.equals("unique violation")) {
//                                                if (field.equals("name")) {
//                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
//                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("This name is already taken.");
//                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
//                                                }
//                                                else {
//                                                    Shared.collapseKeyBoard(RoomFragment.this);
//                                                    dialog.dismiss();
//                                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
//                                                    break;
//                                                }
//                                            }
//                                            else {
//                                                Shared.collapseKeyBoard(RoomFragment.this);
//                                                dialog.dismiss();
//                                                Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
//                                                break;
//                                            }
//                                        }
//
//                                    } catch (JSONException e) {
//                                        Shared.collapseKeyBoard(RoomFragment.this);
//                                        dialog.dismiss();
//                                        Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                                break;
//                                default: {
//                                    Shared.collapseKeyBoard(RoomFragment.this);
//                                    dialog.dismiss();
//                                    Snackbar.make(RoomFragment_LinearLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                    });
//                }
//
//
//            }
//
//            @Override
//            public void onNegative(AlertDialog dialog) {
//                Shared.collapseKeyBoard(RoomFragment.this);
//                dialog.dismiss();
//            }
//        });
//
//
//    }
}
