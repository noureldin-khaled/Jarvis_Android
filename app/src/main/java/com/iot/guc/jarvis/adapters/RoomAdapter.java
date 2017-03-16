package com.iot.guc.jarvis.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(isLastChild){
            convertView = LayoutInflater.from(context).inflate(R.layout.button_add_device, parent, false);
            Button DeviceButton_Button_addDevice = (Button) convertView.findViewById(R.id.DeviceButton_Button_addDevice);
            DeviceButton_Button_addDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                fragment.scanDevices(groupPosition);
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
                    fragment.deleteDevice((Device)getChild(groupPosition,childPosition));
                }
            });

            Button RoomsListItem_Button_Edit = (Button) convertView.findViewById(R.id.RoomsListItem_Button_Edit);
            RoomsListItem_Button_Edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.editDevice((Device)getChild(groupPosition,childPosition));
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
            }
        });

        Button edit = (Button) convertView.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.editRoom(groupPosition);
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
