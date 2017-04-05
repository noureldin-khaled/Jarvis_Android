package com.iot.guc.jarvis.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;

public class ContainerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, container, false);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (Shared.getSelectedRoom() == -1)
            transaction.replace(R.id.ContainerFragment_FrameLayout_Container, new RoomFragment());
        else
            transaction.replace(R.id.ContainerFragment_FrameLayout_Container, new DeviceFragment());

        transaction.commit();
        return view;
    }
}
