package com.iot.guc.jarvis.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.fragments.DeviceFragment;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.responses.DeviceResponse;

import java.util.ArrayList;

public class DeviceAdapter extends BaseAdapter {
    private ArrayList<Device> devices;
    private Context context;
    private DeviceFragment fragment;

    public DeviceAdapter(Context context, ArrayList<Device> devices, DeviceFragment fragment) {
        this.devices = devices;
        this.context = context;
        this.fragment = fragment;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_device, parent, false);

        ImageView DeviceListItem_RelativeLayout_DeviceType = (ImageView) convertView.findViewById(R.id.DeviceListItem_RelativeLayout_DeviceType);
        if (device.getType() == Device.TYPE.LIGHT_BULB)
            DeviceListItem_RelativeLayout_DeviceType.setImageResource(R.drawable.ic_bulb);

        TextView DeviceListItem_TextView_DeviceName = (TextView) convertView.findViewById(R.id.DeviceListItem_TextView_DeviceName);
        DeviceListItem_TextView_DeviceName.setText(device.getName());

        final Switch DeviceListItem_Switch_Toggle = (Switch) convertView.findViewById(R.id.DeviceListItem_Switch_Toggle);
        DeviceListItem_Switch_Toggle.setChecked(device.isStatus());

        DeviceListItem_Switch_Toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                DeviceListItem_Switch_Toggle.setEnabled(false);
                fragment.handleDevice(position, isChecked, new DeviceResponse() {
                    @Override
                    public void onSuccess() {
                        DeviceListItem_Switch_Toggle.setEnabled(true);
                    }

                    @Override
                    public void onFailure() {
                        DeviceListItem_Switch_Toggle.setEnabled(true);
                        DeviceListItem_Switch_Toggle.setChecked(!isChecked);
                    }
                });
            }
        });

        return convertView;
    }
}
