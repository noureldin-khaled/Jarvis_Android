package com.iot.guc.jarvis.fragments;


import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.adapters.PatternsAdapter;
import com.iot.guc.jarvis.models.Event;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.responses.PopupResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PatternsFragment extends Fragment {

    private ArrayList<ArrayList<Event>> Patterns = new ArrayList<ArrayList<Event>>();
    private ProgressBar progressBar;
    private LinearLayout Patterns_LinearLayout;
    private TextView noPatterns;
    private PatternsAdapter patternsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patterns,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.Patterns_RecyclerView_list);

        progressBar = (ProgressBar) view.findViewById(R.id.Patterns_ProgressBar);
        noPatterns = (TextView) view.findViewById(R.id.Patterns_TextView_NoPatterns);
        Patterns_LinearLayout = (LinearLayout) view.findViewById(R.id.Patterns_LinearLayout);

        progressBar.setVisibility(View.VISIBLE);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        fillData();
        patternsAdapter = new PatternsAdapter(Patterns,getActivity(),this);
        recyclerView.setAdapter(patternsAdapter);

        return view;
    }

    private void fillData() {

        Shared.request(getContext(), Request.Method.GET, "/api/patterns", null, true, new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                try {
                    JSONArray patterns = body.getJSONArray("patterns");
                    ArrayList<ArrayList<Event>> p = new ArrayList<ArrayList<Event>>();
                    for (int i =0; i<patterns.length();i++){
                        JSONArray sequence = patterns.getJSONArray(i);
                        ArrayList<Event> s = new ArrayList<Event>();
                        for (int j =0; j<sequence.length();j++){
                            JSONObject event = sequence.getJSONObject(j);
                            Event e = new Event();
                            e.setTime(event.getString("time"));
                            e.setDevice(event.getString("device"));
                            e.setStatus(event.getBoolean("status"));
                            s.add(e);
                        }
                        p.add(s);
                    }
                    progressBar.setVisibility(View.GONE);
                    Patterns = p;
                    if(p.size()==0)
                        noPatterns.setVisibility(View.VISIBLE);


                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    noPatterns.setVisibility(View.VISIBLE);
                    Snackbar.make(Patterns_LinearLayout,"Something Went Wrong!",Snackbar.LENGTH_SHORT);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                progressBar.setVisibility(View.GONE);
                noPatterns.setVisibility(View.VISIBLE);
                Snackbar.make(Patterns_LinearLayout,"Something Went Wrong!",Snackbar.LENGTH_SHORT);
            }
        });

//        Event ev1 = new Event();
//        ev1.setDevice("D1");
//        ev1.setTime("12:00");
//        ev1.setStatus(false);
//        Event ev2 = new Event();
//        ev2.setDevice("D2");
//        ev2.setTime("12:02");
//        ev2.setStatus(true);
//        Event ev3 = new Event();
//        ev3.setDevice("D3");
//        ev3.setTime("01:00");
//        ev3.setStatus(false);
//
//        ArrayList<Event> s = new ArrayList<Event>();
//        s.add(ev1);
//        s.add(ev2);
//        s.add(ev3);
//
//        Patterns.add(s);




    }

    public void editPattern(final int sequence, final int event){

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_add_room, null);

        final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
        final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
        final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
        final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);
        final TextView AddRoomDialog_TextView_Title = (TextView) contentView.findViewById(R.id.AddRoomDialog_TextView_Title);
        AddRoomDialog_TextView_Title.setText("Edit Event Time");

        new Popup().create(getActivity(), contentView, "Edit", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog){
                AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
                AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);

                if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please enter event time");
                }

                if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

            }
            Patterns.get(sequence).get(event).setTime(AddRoomDialog_EditText_RoomName.getText().toString());
            patternsAdapter.notifyDataSetChanged();
            JSONObject body = new JSONObject();
                try {
                    body.put("time",AddRoomDialog_EditText_RoomName.getText().toString());
                } catch (JSONException e) {
                    Snackbar.make(Patterns_LinearLayout,"Something Went Wrong!",Snackbar.LENGTH_SHORT);
                    e.printStackTrace();
                }
                Shared.request(getContext(), Request.Method.PUT, "/api/patterns/" + sequence + "/" + event, body, true, new HTTPResponse() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject body) {
                        PatternsFragment.this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        dialog.dismiss();
                        if(statusCode==200){
                            Snackbar.make(Patterns_LinearLayout,"Edited Successfully!",Snackbar.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, JSONObject body) {
                        Snackbar.make(Patterns_LinearLayout,"Something Went Wrong!",Snackbar.LENGTH_SHORT);
                    }
                });

        }

            @Override
            public void onNegative(AlertDialog dialog) {

            }

        });
    }

}

