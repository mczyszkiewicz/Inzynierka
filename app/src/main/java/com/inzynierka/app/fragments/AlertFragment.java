package com.inzynierka.app.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.inzynierka.app.R;

public class AlertFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.alertfragment,container,false);
        return v;
    }
}
