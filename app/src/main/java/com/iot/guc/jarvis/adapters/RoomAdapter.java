package com.iot.guc.jarvis.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.fragments.RoomFragment;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomAdapter extends BaseExpandableListAdapter {
    private Activity activity;
    private RoomFragment fragment;
    private Context context;
    private ArrayList<String> roomsList;
    private HashMap<String, ArrayList<Device>> devicesList;

    public RoomAdapter(Context context, Activity activity, RoomFragment fragment) {
        this.activity = activity;
        this.context = context;
        this.fragment = fragment;
        roomsList = new ArrayList<>();
        devicesList = new HashMap<>();
    }

    public void refresh(ArrayList<String> rooms, HashMap<String, ArrayList<Device>> devices) {
        roomsList = rooms;
        devicesList = devices;
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.button_add_device, parent, false);
            Button DeviceButton_Button_addDevice = (Button) convertView.findViewById(R.id.DeviceButton_Button_addDevice);
            DeviceButton_Button_addDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                fragment.addDevice(groupPosition);
                }
            });
        }
        else {
            final Device device = (Device) getChild(groupPosition, childPosition);

            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_rooms, parent, false);
            TextView RoomsListItem_TextView_DeviceName = (TextView) convertView.findViewById(R.id.RoomsListItem_TextView_DeviceName);

            RoomsListItem_TextView_DeviceName.setText(device.getName());

            Switch RoomsListItem_Switch_DeviceStatus = (Switch) convertView.findViewById(R.id.RoomsListItem_Switch_DeviceStatus);
            RoomsListItem_Switch_DeviceStatus.setChecked(device.isStatus());
            RoomsListItem_Switch_DeviceStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton sw, boolean isChecked) {
                    if (isChecked) {
                        Shared.turnOnDevice(device.getId());
                    } else Shared.turnOffDevice(device.getId());
                }
            });
            TextView RoomsListItem_TextView_DeviceType = (TextView) convertView.findViewById(R.id.RoomsListItem_TextView_DeviceType);
            RoomsListItem_TextView_DeviceType.setText(device.getType().toString());

            TextView RoomsListItem_TextView_DeleteDevice = (TextView) convertView.findViewById(R.id.RoomsListItem_TextView_DeleteDevice);
            RoomsListItem_TextView_DeleteDevice.setOnClickListener(new View.OnClickListener() {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group_rooms, parent,false);
        }

        Button delete  = (Button) convertView.findViewById(R.id.delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.deleteRoom(groupPosition);
//                new Confirmation().create(activity, "Are you sure you want to delete this room?", "Confirmation", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        deleteRoom(groupPosition);
//                    }
//                }, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).show();
            }
        });

        Button edit = (Button) convertView.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_room,null);

//                final TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
//                title.setText("Edit Room");
//                final TextInputLayout layout = (TextInputLayout) dialogView.findViewById(R.id.layout_name);
//                final EditText name = (EditText) dialogView.findViewById(R.id.name);
//                name.setHint("Room Name");
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
