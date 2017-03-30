package com.iot.guc.jarvis.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.controllers.PeopleActivity;
import com.iot.guc.jarvis.models.User;

import java.util.ArrayList;

public class PeopleAdapter extends BaseAdapter {
    private ArrayList<User> people;
    private PeopleActivity activity;
    private LayoutInflater inflater;

    public PeopleAdapter(PeopleActivity activity, ArrayList<User> people) {
        this.activity = activity;
        this.people = people;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int position) {
        return people.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        User person = people.get(position);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item_person, null);

        ImageView PersonListItem_ImageView_Type = (ImageView) convertView.findViewById(R.id.PersonListItem_ImageView_Type);
        TextView PersonListItem_TextView_Username = (TextView) convertView.findViewById(R.id.PersonListItem_TextView_Username);
        ImageButton PersonListItem_ImageButton_ActionButton = (ImageButton) convertView.findViewById(R.id.PersonListItem_ImageButton_ActionButton);

        PersonListItem_TextView_Username.setText(person.getUsername());
        if (person.getType().equals("Admin")) {
            PersonListItem_ImageView_Type.setImageResource(R.drawable.admin);
            PersonListItem_ImageButton_ActionButton.setImageResource(R.drawable.ic_remove_admin);
        }
        else {
            PersonListItem_ImageView_Type.setImageResource(R.drawable.normal);
            PersonListItem_ImageButton_ActionButton.setImageResource(R.drawable.ic_add_admin);
        }

        PersonListItem_ImageButton_ActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.updateAuth((User) getItem(position));
            }
        });

        return convertView;
    }
}
