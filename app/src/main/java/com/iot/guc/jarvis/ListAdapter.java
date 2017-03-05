package com.iot.guc.jarvis;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by Ahmed Amir on 3/5/2017.
 */

public class ListAdapter extends ArrayAdapter {

    ArrayList<String> items= new ArrayList<String>();
    Context context;
    public ListAdapter(@NonNull Context context, ArrayList<String> data) {
        super(context, 0,data);
        items = data;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View itemView;
        TextView text;
        if(convertView!=null)
            return  convertView;

        itemView =  LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        text = (TextView) itemView.findViewById(R.id.text);
        String roomName =items.get(position);

        text.setText(roomName);
        return itemView;

    }
}
