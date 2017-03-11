package com.iot.guc.jarvis;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class RoomAdapter extends BaseExpandableListAdapter {


    private Context context;
    private List<String> roomsList; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Device>> deviceLists;

    public RoomAdapter(Context context, List<String> listDataHeader,
                       HashMap<String, List<Device>> listChildData) {
        this.context = context;
        this.roomsList = listDataHeader;
        this.deviceLists = listChildData;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.deviceLists.get(this.roomsList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition,  int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(isLastChild){
            convertView = LayoutInflater.from(context).inflate(R.layout.add_device_button, parent, false);
            Button b = (Button) convertView.findViewById(R.id.add_device);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add,null);
                    final EditText entered_name = (EditText) dialogView.findViewById(R.id.name);
                    TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
                    title.setText("Enter Device Name");
                    builder.setView(dialogView);
                    builder.setPositiveButton("Add",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Shared.addDevice(groupPosition,entered_name.getText().toString());

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
            return convertView;
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
            return convertView;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.deviceLists.get(this.roomsList.get(groupPosition)).size()+1;
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent,false);
        }

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
