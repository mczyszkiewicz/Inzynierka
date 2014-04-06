package com.inzynierka.app.activites;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inzynierka.app.R;
import com.inzynierka.app.fragments.ShowFragment;
import com.inzynierka.app.gps.GPSLocation;
import com.inzynierka.app.services.DataService;

import java.util.Calendar;

public class TrasyActivity extends FragmentActivity {



    private GPSLocation gps;
    private String brama_portowa_text;
    private String eskadrowa_text;
    private String gdanska_txt;
    private String szosa_txt;
    private String struga_text;
    private int czas_brama_portowa;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private static double brama_latitude = 14.54829;
    private static double brama_longitude = 53.4249;
    private static double gdanska_longitude = 53.41528;
    private static double gdanska_latitude = 14.56944;
    private float cos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasy);
        Calendar cal = Calendar.getInstance();
        Intent i = new Intent(this, DataService.class);
        pendingIntent = PendingIntent.getService(this,0,i,0);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),30*1000,pendingIntent);
        gps = new GPSLocation(TrasyActivity.this);
        cos = LocationCreator();
        if (messageReceiver !=null)
        {
            IntentFilter intentFilter = new IntentFilter(getString(R.string.data));
            registerReceiver(messageReceiver,intentFilter);
        }
        final ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<?> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.trasy));
        listView.setAdapter(adapter);




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = (String) listView.getItemAtPosition(i);
                int p = (int) listView.getItemIdAtPosition(i);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.position), s);
                String tmp_brama = time_brama_portowa(brama_portowa_text);
                czas_brama_portowa = parsing_brama_portowa(tmp_brama);
                bundle.putString(getString(R.string.brama), brama_portowa_text);
                bundle.putString(getString(R.string.eskadrowa), eskadrowa_text);
                bundle.putString(getString(R.string.struga), struga_text);
                bundle.putString(getString(R.string.gdanska), gdanska_txt);
                bundle.putString(getString(R.string.szosa), szosa_txt);
                bundle.putInt(getString(R.string.eskadrowa_most),time_eskadrowa_most(eskadrowa_text));
                bundle.putInt(getString(R.string.eskadrowa_trasa), time_eskadrowa_trasa(eskadrowa_text));
                bundle.putInt(getString(R.string.struga_os_reda), time_os_reda(struga_text));
                bundle.putInt(getString(R.string.struga_most_dlugi), time_struga_most_dlugi(struga_text));
                bundle.putInt(getString(R.string.struga_trasa_zamkowa), time_struga_trasa_zamkowa(struga_text));
                bundle.putInt(getString(R.string.time_brama_portowa), czas_brama_portowa);
                bundle.putInt(getString(R.string.gdanska_most), time_gdanska_most(gdanska_txt));

                bundle.putInt(getString(R.string.szosa_reda), time_szosa_os_reda(szosa_txt));
                bundle.putInt(getString(R.string.szosa_most), time_szosa_most_dlugi(szosa_txt));
                bundle.putInt(getString(R.string.szosa_trasa), time_szosa_trasa_zamkowa(szosa_txt));

                bundle.putInt(getString(R.string.place), p);
                FragmentManager fm = getSupportFragmentManager();
                ShowFragment showFragment = new ShowFragment();
                showFragment.setArguments(bundle);
                showFragment.show(fm,"yup");


            }
        });

    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            eskadrowa_text = intent.getStringExtra(getString(R.string.eskadrowa));
            brama_portowa_text = intent.getStringExtra(getString(R.string.brama));
            gdanska_txt = intent.getStringExtra(getString(R.string.gdanska));
            struga_text = intent.getStringExtra(getString(R.string.struga));
            szosa_txt = intent.getStringExtra(getString(R.string.szosa));
        }
    };


    public float LocationCreator() {


        Location moja;
        double latitude;
        double longitude;
        float distance = 0;
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            moja = new Location("moja");
            moja.setLatitude(latitude);
            moja.setLongitude(longitude);
            Toast.makeText(getBaseContext(), "z" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();

            Location gdanska = new Location(getString(R.string.gdanska));
            gdanska.setLatitude(gdanska_latitude);
            gdanska.setLongitude(gdanska_longitude);

            Location loc = new Location("point A");
            loc.setLatitude(brama_latitude);
            loc.setLongitude(brama_longitude);
            distance = loc.distanceTo(gdanska);
           int cos = Math.round(distance);
            Log.d("appka", "z" + cos);

        }
        return distance;
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
        stopUsingService();


    }

    private void stopUsingService()
    {
        alarmManager.cancel(pendingIntent);
        Intent intent = new Intent(this,DataService.class);
        stopService(intent);
    }



    public int time_eskadrowa_most(String tmp) {
        tmp = tmp.substring(11, 13);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }

    public int time_eskadrowa_trasa(String tmp) {

        tmp = tmp.substring(29, 31);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);


    }

    public int time_os_reda(String tmp) {
        tmp = tmp.substring(9, 11);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }

    public int time_struga_most_dlugi(String tmp) {
        tmp = tmp.substring(63, 66);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }

    public int time_struga_trasa_zamkowa(String tmp) {
        tmp = tmp.substring(32, 35);
        tmp = tmp.replaceAll(getString(R.string.pattern), "");

        return Integer.parseInt(tmp);

    }
    public int time_gdanska_most(String tmp) {
        tmp = tmp.substring(10, 14);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }


    public int time_szosa_os_reda(String tmp) {
        tmp = tmp.substring(9, 12);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        tmp = tmp.trim();

        return Integer.parseInt(tmp);

    }

    public int time_szosa_most_dlugi(String tmp) {
        tmp = tmp.substring(63, 67);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }

    public int time_szosa_trasa_zamkowa(String tmp) {
        tmp = tmp.substring(32, 35);
        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);
    }
    public String time_brama_portowa(String tmp) {
        tmp = tmp.substring(23, 26);
        tmp = tmp.trim();
        return tmp;
    }

    public int parsing_brama_portowa(String tmp) {

        tmp = tmp.replaceAll((getString(R.string.pattern)), "");

        return Integer.parseInt(tmp);

    }

}