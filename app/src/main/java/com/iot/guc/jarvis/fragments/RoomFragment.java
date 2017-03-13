package com.iot.guc.jarvis.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.PopupResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
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
    private ExpandableListView expListView;
    private TextView roomsLabel;
    private LinearLayout main_layout;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        Button b = (Button) view.findViewById(R.id.add_room);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View contentView = inflater.inflate(R.layout.dialog_add_room, null);
                final TextInputLayout layout_name = (TextInputLayout) contentView.findViewById(R.id.layout_name);
                final EditText name = (EditText) contentView.findViewById(R.id.name);
                final ProgressBar progress = (ProgressBar) contentView.findViewById(R.id.progress);
                final LinearLayout form_add_room = (LinearLayout) contentView.findViewById(R.id.form_add_room);

                new Popup().create(getActivity(), contentView, "Add", new PopupResponse() {
                    @Override
                    public void onPositive(final AlertDialog dialog) {
                        layout_name.setErrorEnabled(false);
                        layout_name.setError(null);

                        if (name.getText().toString().isEmpty()) {
                            layout_name.setErrorEnabled(true);
                            layout_name.setError("Please Enter a Room Name");
                        }

                        if (!name.getText().toString().isEmpty()) {
                            progress.setVisibility(View.VISIBLE);
                            form_add_room.setVisibility(View.GONE);
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                            Room.addRoom(getContext(), name.getText().toString(), new HTTPResponse() {
                                @Override
                                public void onSuccess(int statusCode, JSONObject body) {
                                    Shared.collapseKeyBoard(RoomFragment.this);
                                    dialog.dismiss();

                                    try {
                                        JSONObject jsonRoom = body.getJSONObject("room");
                                        Shared.addRoom(new Room(jsonRoom.getInt("id") , jsonRoom.getString("name")));
                                        refill();
                                        Snackbar.make(main_layout, "Room Created Successfully", Snackbar.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, JSONObject body) {
                                    progress.setVisibility(View.GONE);
                                    form_add_room.setVisibility(View.VISIBLE);
                                    switch (statusCode) {
                                        case Constants.NO_INTERNET_CONNECTION: {
                                            Shared.collapseKeyBoard(RoomFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(main_layout, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case Constants.SERVER_NOT_REACHED: {
                                            Shared.collapseKeyBoard(RoomFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(main_layout, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG).show();
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
                                                            layout_name.setErrorEnabled(true);
                                                            layout_name.setError("Please Enter a Room Name");
                                                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                        }
                                                        else {
                                                            Shared.collapseKeyBoard(RoomFragment.this);
                                                            dialog.dismiss();
                                                            Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                            break;
                                                        }
                                                    }
                                                    else if (type.equals("unique violation")) {
                                                        if (field.equals("name")) {
                                                            layout_name.setErrorEnabled(true);
                                                            layout_name.setError("This name is already taken.");
                                                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                        }
                                                        else {
                                                            Shared.collapseKeyBoard(RoomFragment.this);
                                                            dialog.dismiss();
                                                            Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                            break;
                                                        }
                                                    }
                                                    else {
                                                        Shared.collapseKeyBoard(RoomFragment.this);
                                                        dialog.dismiss();
                                                        Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                    }
                                                }

                                            } catch (JSONException e) {
                                                Shared.collapseKeyBoard(RoomFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }

                                        }
                                        break;
                                        default: {
                                            Shared.collapseKeyBoard(RoomFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(main_layout, "Something Went Wrong!", Snackbar.LENGTH_LONG).show();
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
        });

        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);
        roomsLabel = (TextView) view.findViewById(R.id.list_label);
        main_layout = (LinearLayout) view.findViewById(R.id.main_layout);
        roomAdapter = new RoomAdapter(getActivity().getApplicationContext(), getActivity());
        refill();
        expListView.setAdapter(roomAdapter);

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
            roomsLabel.setText("No Rooms Found");
        else
            roomsLabel.setText("Rooms");

        roomAdapter.refresh(rooms, devices);
    }
}
