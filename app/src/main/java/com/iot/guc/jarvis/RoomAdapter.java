package com.iot.guc.jarvis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAdapter extends BaseExpandableListAdapter {
    private Activity activity;
    private Context context;
    private ArrayList<String> roomsList;
    private HashMap<String, List<Device>> devicesList;
    private TextView roomsLabel;

    public RoomAdapter(Context context, Activity activity, TextView roomsLabel) {
        this.activity = activity;
        this.context = context;
        this.roomsLabel = roomsLabel;
        refresh();
    }

    public void refresh() {
        roomsList = new ArrayList<>();
        HashMap<Integer, ArrayList<Device>> hm = new HashMap<>();
        for (int i = 0; i < Shared.getRooms().size(); i++) {
            roomsList.add(Shared.getRooms().get(i).getName());
            hm.put(Shared.getRooms().get(i).getId(), new ArrayList<Device>());
        }

        devicesList = new HashMap<>();
        for (Device device : Shared.getDevices()) {
            ArrayList<Device> list = hm.get(device.getRoom_id());
            list.add(device);
            hm.put(device.getRoom_id(), list);
        }

        for (int i = 0; i < Shared.getRooms().size(); i++)
            devicesList.put(Shared.getRooms().get(i).getName(), hm.get(Shared.getRooms().get(i).getId()));

        if (Shared.getRooms().isEmpty())
            roomsLabel.setText("No Rooms Found");
        else
            roomsLabel.setText("Rooms");


        notifyDataSetChanged();
    }

    public void addDevice(final String name, final TextInputLayout layout, final AlertDialog dialog, final Room room) {
        if (name.isEmpty()) return;

        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/device";
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("type", "Light Bulb");
            body.put("mac", "E7:36:D8:F7:F9:A0");
            body.put("ip", "192.168.1.3");
            body.put("room_id", room.getId());

            final ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Creating Device...");
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonDevice = response.getJSONObject("device");
                        Shared.addDevice(new Device(jsonDevice.getInt("id"), jsonDevice.getString("name"), jsonDevice.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK, jsonDevice.getBoolean("status"), jsonDevice.getString("mac"), jsonDevice.getString("ip"), jsonDevice.getInt("room_id")));
                        refresh();
                        dialog.dismiss();

                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        Toast.makeText(activity, "Device Created Successfully.", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        e.printStackTrace();
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 500) {
                            new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                            Log.e("FLT", "onErrorResponse: " + new String(error.networkResponse.data, "UTF-8"));
                        } else {
                            String err = new String(error.networkResponse.data, "UTF-8");
                            Log.e("FLT", "onErrorResponse: " + err);
//                            JSONObject json = new JSONObject(err);
//                            JSONArray arr = json.getJSONArray("errors");
//
//                            for (int i = 0; i < arr.length(); i++) {
//                                JSONObject current = arr.getJSONObject(i);
//                                String type = current.getString("msg");
//                                String field = current.getString("param");
//
//                                if (type.equals("required")) {
//                                    if (field.equals("name")) {
//                                        layout.setErrorEnabled(true);
//                                        layout.setError("Please Enter a Device Name");
//                                    } else {
//                                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
//                                        break;
//                                    }
//                                } else if (type.equals("unique violation")) {
//                                    if (field.equals("name")) {
//                                        layout.setErrorEnabled(true);
//                                        layout.setError("This name is already taken.");
//                                    } else {
//                                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
//                                        break;
//                                    }
//                                } else {
//                                    new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
//                                    break;
//                                }
//                            }
                        }
                    } catch (Exception e) {
                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        e.printStackTrace();
                    }

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
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
            new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
            e.printStackTrace();
        }
    }

    public void editRoom(final String name, final int roomPosition, final AlertDialog dialog){
        if (name.isEmpty()) return;

        try {
            final Room r = Shared.getRooms().get(roomPosition);
            RequestQueue q = Volley.newRequestQueue(context);
            String url = Shared.getServer().URL() + "/api/room/" + r.getId();
            JSONObject body = new JSONObject();
            body.put("name", name);

            final ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Saving Changes...");
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing())
                progressDialog.show();

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Shared.editRoom(roomPosition, new Room(r.getId(), name));
                    refresh();

                    dialog.dismiss();
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (error.networkResponse.statusCode == 404)
                            new Error().create(activity, "The Room was not found!", "Opss").show();
                        else
                            new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    } catch (Exception e) {
                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Shared.getAuth().getToken());
                    return headers;
                }
            };

            q.add(req);
        } catch (JSONException e) {
            new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
        }
    }

    public void deleteRoom(final int roomPosition){
        final Room r = Shared.getRooms().get(roomPosition);
        RequestQueue q = Volley.newRequestQueue(context);
        String url = Shared.getServer().URL() + "/api/room/"+r.getId();
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Deleting Room...");
        progressDialog.setCancelable(false);
        if(!progressDialog.isShowing())
            progressDialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Shared.removeRoom(roomPosition);
                ArrayList<Integer> indices = new ArrayList<Integer>();
                for (int i = Shared.getDevices().size()-1; i>=0;i--)
                    if( Shared.getDevices().get(i).getRoom_id() == r.getId())
                        indices.add(i);

                for (int i : indices)
                    Shared.removeDevice(i);

                refresh();
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse.statusCode == 404)
                        new Error().create(activity, "The Room was not found!", "Opss").show();
                    else
                        new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (Exception e) {
                    new Error().create(activity, "Opss.. Something Went Wrong.\nPlease type that again.", "Opss").show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", Shared.getAuth().getToken());
                return headers;
            }
        };

        q.add(req);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.devicesList.get(this.roomsList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(isLastChild){
            convertView = LayoutInflater.from(context).inflate(R.layout.add_device_button, parent, false);
            Button b = (Button) convertView.findViewById(R.id.add_device);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Room room = Shared.getRooms().get(groupPosition);
                    View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add, null);

                    final TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
                    title.setText("Add a Device");
                    final TextInputLayout layout = (TextInputLayout) dialogView.findViewById(R.id.layout_name);
                    final EditText name = (EditText) dialogView.findViewById(R.id.name);
                    name.setHint("Device Name");

//                    final AlertDialog dialog = new Popup().create(activity, dialogView, "Add");
//                    dialog.show();
//
//                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (name.getText().toString().isEmpty()) {
//                                layout.setErrorEnabled(true);
//                                layout.setError("Please Enter a Device Name");
//                            }
//                            else {
//                                layout.setErrorEnabled(false);
//                                layout.setError(null);
//                            }
//
//                            addDevice(name.getText().toString(), layout, dialog, room);
//                        }
//                    });
//
//                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                            dialog.dismiss();
//                        }
//                    });
                }
            });
        }
        else {
            final Device device = (Device) getChild(groupPosition, childPosition);

            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);

            deviceName.setText(device.getName());

            Switch status = (Switch) convertView.findViewById(R.id.toggle);
            status.setChecked(device.isStatus());
            status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton sw, boolean isChecked) {
                    if (isChecked) {
                        Shared.turnOnDevice(device.getId());
                    } else Shared.turnOffDevice(device.getId());
                }
            });
            TextView deviceType = (TextView) convertView.findViewById(R.id.type);
            deviceType.setText(device.getType().toString());

            TextView delete = (TextView) convertView.findViewById(R.id.delete_device);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Shared.deleteDevice(device.getId());
                }
            });
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.devicesList.get(this.roomsList.get(groupPosition)).size()+1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.roomsList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.roomsList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent,false);
        }

        Button delete  = (Button) convertView.findViewById(R.id.delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Confirmation().create(activity, "Are you sure you want to delete this room?", "Confirmation", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRoom(groupPosition);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        });

        Button edit = (Button) convertView.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add,null);

                final TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
                title.setText("Edit Room");
                final TextInputLayout layout = (TextInputLayout) dialogView.findViewById(R.id.layout_name);
                final EditText name = (EditText) dialogView.findViewById(R.id.name);
                name.setHint("Room Name");
//                final AlertDialog dialog = new Popup().create(activity,dialogView,"Save");
//
//                dialog.show();
//
//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (name.getText().toString().isEmpty()) {
//                            layout.setErrorEnabled(true);
//                            layout.setError("Please Enter a Room Name");
//                        }
//                        else {
//                            layout.setErrorEnabled(false);
//                            layout.setError(null);
//                        }
//
//                        editRoom(name.getText().toString(), groupPosition, dialog);
//                    }
//                });
//
//                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                        dialog.dismiss();
//                    }
//                });

            }
        });

        TextView listHeader = (TextView) convertView.findViewById(R.id.list_header);
        listHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
