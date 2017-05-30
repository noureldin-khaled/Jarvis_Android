package com.iot.guc.jarvis.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.models.Device;

import java.util.ArrayList;

public class ScannedDeviceAdapter extends BaseAdapter {
    private ArrayList<Device> devices;
    private LayoutInflater inflater;

    public ScannedDeviceAdapter(Activity activity, ArrayList<Device> devices) {
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
            convertView = inflater.inflate(R.layout.list_item_scanned_device, null);

        ImageView ScannedDeviceListItem_ImageView_DeviceType = (ImageView) convertView.findViewById(R.id.ScannedDeviceListItem_ImageView_DeviceType);
        if (device.getType() == Device.TYPE.LIGHT_BULB)
            ScannedDeviceListItem_ImageView_DeviceType.setImageResource(R.drawable.ic_bulb);

        TextView ScannedDeviceListItem_TextView_DeviceMac = (TextView) convertView.findViewById(R.id.ScannedDeviceListItem_TextView_DeviceMac);
        ScannedDeviceListItem_TextView_DeviceMac.setText(device.getMac().toUpperCase());

        return convertView;
    }
}
