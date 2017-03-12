package com.iot.guc.jarvis;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomFragment extends Fragment {
    private RoomAdapter roomAdapter;
    private ExpandableListView expListView;
    private TextView roomsLabel;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        Button b = (Button) view.findViewById(R.id.add_room);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View contentView = inflater.inflate(R.layout.dialog_add, null);

                final TextView title = (TextView) contentView.findViewById(R.id.dialog_title);
                title.setText("Add a Room");
                final TextInputLayout layout = (TextInputLayout) contentView.findViewById(R.id.layout_name);
                final EditText name = (EditText) contentView.findViewById(R.id.name);
                name.setHint("Room Name");

                final AlertDialog dialog = new Popup().create(getActivity(), contentView, "Add");
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (name.getText().toString().isEmpty()) {
                            layout.setErrorEnabled(true);
                            layout.setError("Please Enter a Room Name");
                        }
                        else {
                            layout.setErrorEnabled(false);
                            layout.setError(null);
                        }

                        addRoom(name.getText().toString(), layout, dialog);
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        dialog.dismiss();
                    }
                });
            }
        });

        expListView = (ExpandableListView) view.findViewById(R.id.exp_list);
        roomsLabel = (TextView) view.findViewById(R.id.list_label);
        if (Shared.getRooms().isEmpty())
            roomsLabel.setText("No Rooms Found");
        else
            roomsLabel.setText("Rooms");

//        prepareDummyData();
        roomAdapter = new RoomAdapter(getActivity().getApplicationContext(), getActivity());
        expListView.setAdapter(roomAdapter);

        return view;
    }

//    private void prepareDummyData() {
//        ArrayList list = new ArrayList();
//
//        roomList.add("Bathroom");
//        roomList.add("Kitchen");
//        roomList.add("Bedroom");
//
//        for(int i=0;i<4;i++){
//            Device d = new Device(i,"Device"+i, Device.TYPE.LIGHT_BULB,true,"","",1);
//            list.add(d);
//        }
//        deviceList.put("Bathroom",list);
//        list = new ArrayList();
//        for(int i=4;i<8;i++){
//            Device d = new Device(i,"Device"+i, Device.TYPE.LIGHT_BULB,true,"","",2);
//            list.add(d);
//        }
//        deviceList.put("Kitchen",list);
//        list = new ArrayList();
//        deviceList.put("Bedroom",list);
//    }

    public void addRoom(final String name, final TextInputLayout layout, final AlertDialog dialog) {
        if (name.isEmpty()) return;

        try {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Shared.getServer().URL() + "/api/room";
            JSONObject body = new JSONObject();
            body.put("name", name);

            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Creating Room...");
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonRoom = response.getJSONObject("room");
                        Shared.addRoom(new Room(jsonRoom.getInt("id") , jsonRoom.getString("name")));
                        roomAdapter.refresh();
                        dialog.dismiss();

                        if (Shared.getRooms().isEmpty())
                            roomsLabel.setText("No Rooms Found");
                        else
                            roomsLabel.setText("Rooms");

                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        Toast.makeText(getActivity(), "Room Created Successfully.", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 500) {
                            new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        } else {
                            String err = new String(error.networkResponse.data, "UTF-8");
                            JSONObject json = new JSONObject(err);
                            JSONArray arr = json.getJSONArray("errors");

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject current = arr.getJSONObject(i);
                                String type = current.getString("msg");
                                String field = current.getString("param");

                                if (type.equals("required")) {
                                    if (field.equals("name")) {
                                        layout.setErrorEnabled(true);
                                        layout.setError("Please Enter a Room Name");
                                    }
                                    else {
                                        new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else if (type.equals("unique violation")) {
                                    if (field.equals("name")) {
                                        layout.setErrorEnabled(true);
                                        layout.setError("This name is already taken.");
                                    }
                                    else {
                                        new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                        break;
                                    }
                                }
                                else {
                                    new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                                    break;
                                }
                            }
                        }

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    } catch (Exception e) {
                        new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Shared.getAuth().getToken());
                    return headers;
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            new Error().create(getActivity(), "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
        }
    }
}
