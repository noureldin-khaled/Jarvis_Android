package com.iot.guc.jarvis.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.iot.guc.jarvis.AlertService;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.fragments.PatternsFragment;
import com.iot.guc.jarvis.models.Event;

import java.util.ArrayList;
import java.util.Calendar;

public class PatternsAdapter extends RecyclerView.Adapter<PatternsAdapter.ViewHolder> {

    private ArrayList<ArrayList<Event>> sequences;
    private ArrayList<Boolean> auto = new ArrayList<>();
    private Context context;
    private PatternsFragment fragment;

    public PatternsAdapter(ArrayList<ArrayList<Event>>Data, Context context, PatternsFragment fragment){
        sequences = Data;
        this.context = context;
        this.fragment = fragment;
        for (int i = 0; i < Data.size();i++){
            auto.add(false);
        }

    }


    @Override
    public PatternsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.patterns_sequence,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SequenceAdapter sequenceAdapter = new SequenceAdapter(sequences.get(position),fragment,position);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setAdapter(sequenceAdapter);
        holder.checkBox.setChecked(auto.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                for(int i = 0; i<sequences.get(position).size();i++) {

                    String time = sequences.get(position).get(i).getTime();
                    Event e = sequences.get(position).get(i);
                    Shared.setServiceEvents(sequences.get(position));
                    Shared.setServiceCount(0);
                    fragment.setAlarm(time, e, b);
                    if(b){
                        auto.set(position,true);
                    } else {
                        auto.set(position,false);
                    }
                }

            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.deletePattern(position);

            }
        });
    }


    @Override
    public int getItemCount() {
        return sequences.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        CheckBox checkBox;
        ImageView delete;

        public ViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.patterns_sequence);
            checkBox = (CheckBox) itemView.findViewById(R.id.Patterns_Checkbox_Auto);
            delete = (ImageView) itemView.findViewById(R.id.ic_delete);
        }
    }
}
