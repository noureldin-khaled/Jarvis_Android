package com.iot.guc.jarvis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iot.guc.jarvis.R;

import java.util.ArrayList;

public class RoomAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> rooms;

    public RoomAdapter(Context context, ArrayList<String> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String room = (String) getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_room, parent,false);

        TextView RoomListItem_TextView_RoomName = (TextView) convertView.findViewById(R.id.RoomListItem_TextView_RoomName);
        RoomListItem_TextView_RoomName.setText(room);

        return convertView;
    }

    public void add(String room) {
        rooms.add(room);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        rooms.remove(index);
        notifyDataSetChanged();
    }
}
