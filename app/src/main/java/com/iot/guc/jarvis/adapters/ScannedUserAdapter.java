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
import com.iot.guc.jarvis.models.User;

import java.util.ArrayList;

public class ScannedUserAdapter extends BaseAdapter {
    private ArrayList<User> users;
    private LayoutInflater inflater;

    public ScannedUserAdapter(Activity activity, ArrayList<User> users) {
        this.users = users;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        User user = users.get(position);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item_scanned_user, null);

        ImageView ScannedUserListItem_ImageView_UserType = (ImageView) convertView.findViewById(R.id.ScannedUserListItem_ImageView_UserType);
        if (user.getType().equals("Admin"))
            ScannedUserListItem_ImageView_UserType.setImageResource(R.drawable.admin);
        else
            ScannedUserListItem_ImageView_UserType.setImageResource(R.drawable.normal);

        TextView ScannedUserListItem_TextView_Username = (TextView) convertView.findViewById(R.id.ScannedUserListItem_TextView_Username);
        ScannedUserListItem_TextView_Username.setText(user.getUsername());

        return convertView;
    }
}
