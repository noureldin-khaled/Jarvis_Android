package com.iot.guc.jarvis.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.models.Event;

import java.util.ArrayList;


public class SequenceAdapter extends RecyclerView.Adapter<SequenceAdapter.ViewHolder> {

    private ArrayList<Event> Events;

    public SequenceAdapter(ArrayList<Event>Data){
        Events = Data;
    }

    @Override
    public SequenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.patterns_sequence_event,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SequenceAdapter.ViewHolder holder, int position) {
        holder.time.setText(Events.get(position).getTime());
        holder.device.setText(Events.get(position).getDevice());
        String status = "";
        if(Events.get(position).getStatus())
            status = "Turn On";
        else status = "Turn Off";
        holder.status.setText(status);
    }

    @Override
    public int getItemCount() {
        return Events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView time;
        TextView device;
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.event_time);
            device = (TextView) itemView.findViewById(R.id.PatternsFragment_device_name);
            status = (TextView) itemView.findViewById(R.id.PatternsFragment_device_status);
        }
    }
}
