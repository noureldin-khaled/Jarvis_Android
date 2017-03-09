package com.iot.guc.jarvis;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class RoomAdapter extends BaseExpandableListAdapter {


    private Context context;
    private List<String> roomsList; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> deviceLists;

    public RoomAdapter(Context context, List<String> listDataHeader,
                       HashMap<String, List<String>> listChildData) {
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
    public View getChildView(int groupPosition,  int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        TextView listChild = (TextView) convertView.findViewById(R.id.list_item);

        listChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.deviceLists.get(this.roomsList.get(groupPosition)).size();
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
