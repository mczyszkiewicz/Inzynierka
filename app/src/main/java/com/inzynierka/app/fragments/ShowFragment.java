package com.inzynierka.app.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inzynierka.app.R;

public class ShowFragment extends DialogFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        String position = bundle.getString(getString(R.string.position));
        int place = bundle.getInt(getString(R.string.place));

        getDialog().setTitle(position);
        View v = inflater.inflate(R.layout.showfragment,container,false);
       ImageView imageView = (ImageView) v.findViewById(R.id.imageViewfragment);
        TextView textView = (TextView)v.findViewById(R.id.textViewImage);
        switch(place) {

            case 0:

                String tekst_brama = bundle.getString(getString(R.string.brama));
                int czas = bundle.getInt(getString(R.string.time_brama_portowa));


                textView.setText(tekst_brama);
                if (czas < 17) {
                    imageView.setImageResource(R.drawable.brama1);
                }
               else if ((czas >= 17) && (czas < 25)) {
                    imageView.setImageResource(R.drawable.bramazolta);
                }
               else if (czas >= 25) {
                    imageView.setImageResource(R.drawable.bramaczerwona);
                }
                break;


            case 1:

                int czas_most_gdanska = bundle.getInt(getString(R.string.gdanska_most));
                textView.setText(bundle.getString(getString(R.string.gdanska)) + "'");

                if (czas_most_gdanska < 8) {
                    imageView.setImageResource(R.drawable.gdanska_zielona);
                } else if (czas_most_gdanska >= 8 && czas_most_gdanska < 12) {
                    imageView.setImageResource(R.drawable.gdanska_zolta);
                } else if (czas_most_gdanska >= 12) {
                    imageView.setImageResource(R.drawable.gdanskaczerwona);
                }
                break;

            case 2:
                int czas_most = bundle.getInt(getString(R.string.eskadrowa_most));
                String tekst_eskadrowa = bundle.getString(getString(R.string.eskadrowa));
                textView.setText(tekst_eskadrowa);
                if (czas_most < 12) {
                    imageView.setImageResource(R.drawable.eskadrowa_zielona);
                } else if (czas_most >= 12 && czas_most < 15) {
                    imageView.setImageResource(R.drawable.eskadrowamostdlugizolty);
                } else if (czas_most >= 15) {
                    imageView.setImageResource(R.drawable.eskadrowaczerwona);
                }
                break;

            case 3:
                String tekst_struga = bundle.getString(getString(R.string.struga));

                int czas_most_dlugi = bundle.getInt(getString(R.string.struga_most_dlugi));
                textView.setText(tekst_struga);

                if (czas_most_dlugi < 15) {
                    imageView.setImageResource(R.drawable.strugazielone);
                } else if (czas_most_dlugi >= 15 && czas_most_dlugi < 20) {
                    imageView.setImageResource(R.drawable.strugamostdlugizolty);
                } else if (czas_most_dlugi >= 20) {
                    imageView.setImageResource(R.drawable.strugamostczerwone);
                }
                break;
            case 4:

                int czas_red_szosa = bundle.getInt(getString(R.string.szosa_reda));
                int czas_most_dlugi_szosa = bundle.getInt(getString(R.string.szosa_most));
                textView.setText(bundle.getString(getString(R.string.szosa)) + "'");
                if (czas_red_szosa > 20 && czas_red_szosa < 24 && czas_most_dlugi_szosa >= 23 && czas_most_dlugi_szosa < 25) {
                    imageView.setImageResource(R.drawable.szosastargardzkamoststrugazolte);
                } else if (czas_red_szosa >= 25 && czas_most_dlugi_szosa <= 20) {
                    imageView.setImageResource(R.drawable.szosastargardzkaczerwonastruga);
                } else if (czas_red_szosa < 20 && czas_most_dlugi_szosa >= 25) {
                    imageView.setImageResource(R.drawable.szosastargardzkamostczerwony);
                } else if (czas_red_szosa <= 23 && czas_most_dlugi_szosa < 20) {
                    imageView.setImageResource(R.drawable.szosastargardzkazielone);
                }
                else if (czas_most_dlugi_szosa >= 20 && czas_red_szosa >= 20) {
                    imageView.setImageResource(R.drawable.szosamostdlugizolty);
                 }

                break;

        }

        return v;
    }
}

