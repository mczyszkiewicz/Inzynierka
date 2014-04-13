package com.inzynierka.app.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inzynierka.app.R;

/*
 * Created by Mateusz Czyszkiewicz on 2014-04-13.
 */
public class InfoFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.infofragment,container,false);
        Button button = (Button)v.findViewById(R.id.button);

        getDialog().setTitle(getString(R.string.obrobka_danych));
        getDialog().setCanceledOnTouchOutside(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return v;
    }
}
