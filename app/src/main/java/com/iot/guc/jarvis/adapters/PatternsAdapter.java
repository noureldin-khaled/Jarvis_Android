package com.iot.guc.jarvis.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.models.Event;

import java.util.ArrayList;


public class PatternsAdapter extends RecyclerView.Adapter<PatternsAdapter.ViewHolder> {

    private ArrayList<ArrayList<Event>> sequences;
    private Context context;

    public PatternsAdapter(ArrayList<ArrayList<Event>>Data, Context context){
        sequences = Data;
        this.context = context;
    }


    @Override
    public PatternsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.patterns_sequence,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SequenceAdapter sequenceAdapter = new SequenceAdapter(sequences.get(position));
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setAdapter(sequenceAdapter);
    }

    @Override
    public int getItemCount() {
        return sequences.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;


        public ViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.patterns_sequence);
        }
    }
}
