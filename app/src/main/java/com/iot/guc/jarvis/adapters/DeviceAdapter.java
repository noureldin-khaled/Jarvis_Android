package com.iot.guc.jarvis.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.models.Device;

import java.util.ArrayList;

public class DeviceAdapter extends BaseAdapter {
    private ArrayList<Device> devices;
    private LayoutInflater inflater;

    public DeviceAdapter(Activity activity, ArrayList<Device> devices) {
        this.devices = devices;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Device device = devices.get(position);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item_devices, null);
        TextView DevicesListItem_TextView_DeviceType = (TextView) convertView.findViewById(R.id.DevicesListItem_TextView_DeviceType);
        TextView DevicesListItem_TextView_DeviceMac = (TextView) convertView.findViewById(R.id.DevicesListItem_TextView_DeviceMac);

        DevicesListItem_TextView_DeviceType.setText(device.getType().toString());
        DevicesListItem_TextView_DeviceMac.setText(device.getMac());

        return convertView;
    }
}
