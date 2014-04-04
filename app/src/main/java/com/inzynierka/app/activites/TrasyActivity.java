package com.inzynierka.app.activites;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inzynierka.app.R;
import com.inzynierka.app.fragments.ShowFragment;
import com.inzynierka.app.gps.GPSLocation;
import com.inzynierka.app.services.DataService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrasyActivity extends FragmentActivity {

    private static final String https_url = "https://szr.szczecin.pl/utms/data/layers/VMSPublic";

    private GPSLocation gps;
    private    String brama_portowa_text;
    protected  String eskadrowa_text;
    protected  String gdanska_txt;
    protected  String szosa_txt;
    protected  String struga_text;
    protected int czas_brama_portowa;


    private static double brama_latitude = 14.54829;
    private static double brama_longitude = 53.4249;
    private static double gdanska_longitude = 53.41528;
    private static double gdanska_latitude = 14.56944;
    private float cos;
    private PendingIntent pendingIntent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String blah = intent.getStringExtra("brama");
            Toast.makeText(getBaseContext(),"z" + blah,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasy);
        Calendar cal = Calendar.getInstance();
        Intent i = new Intent(this, DataService.class);
        pendingIntent = PendingIntent.getService(this,0,i,0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),30*1000,pendingIntent);


        gps = new GPSLocation(TrasyActivity.this);


        cos = LocationCreator();

        final ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<?> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.trasy));
        listView.setAdapter(adapter);

        if(2000 < cos)
        {
           listView.setBackgroundColor(getResources().getColor(R.color.yellow));
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,new IntentFilter("data"));




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = (String) listView.getItemAtPosition(i);
                int p = (int) listView.getItemIdAtPosition(i);
                Bundle bundle = new Bundle();
                bundle.putString("position", s);
                String tmp_brama = time_brama_portowa(brama_portowa_text);
               czas_brama_portowa = parsing_brama_portowa(tmp_brama);
                view.setBackgroundColor(getResources().getColor(R.color.yellow));

              bundle.putString("brama_portowa", brama_portowa_text);
                bundle.putString("eskadrowa", eskadrowa_text);
                bundle.putString("struga", struga_text);
                bundle.putString("gdanska", gdanska_txt);
                bundle.putString("szosa", szosa_txt);
                bundle.putInt("eskadrowa_most", time_eskadrowa_most(eskadrowa_text));
                bundle.putInt("eskadrowa_trasa", time_eskadrowa_trasa(eskadrowa_text));
                bundle.putInt("struga_os_reda", time_os_reda(struga_text));
                bundle.putInt("struga_most_dlugi", time_struga_most_dlugi(struga_text));
                bundle.putInt("struga_trasa_zamkowa", time_struga_trasa_zamkowa(struga_text));
                bundle.putInt("time_brama_portowa", czas_brama_portowa);
                bundle.putInt("gdanska_most", time_gdanska_most(gdanska_txt));

                bundle.putInt("szosa_reda", time_szosa_os_reda(szosa_txt));
                bundle.putInt("szosa_most", time_szosa_most_dlugi(szosa_txt));
                bundle.putInt("szosa_trasa", time_szosa_trasa_zamkowa(szosa_txt));

                bundle.putInt("place", p);
                FragmentManager fm = getSupportFragmentManager();
                ShowFragment showFragment = new ShowFragment();
                showFragment.setArguments(bundle);
                showFragment.show(fm,"yup");


            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,new IntentFilter(DataService.NOTIFICATION_SERVICE));
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            eskadrowa_text = intent.getStringExtra("eskadrowa");
            brama_portowa_text = intent.getStringExtra("brama");
            gdanska_txt = intent.getStringExtra("gdanska");
            struga_text = intent.getStringExtra("struga");
            szosa_txt = intent.getStringExtra("szosa");
        



        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (messageReceiver !=null)
        {
            IntentFilter intentFilter = new IntentFilter("data");
            registerReceiver(messageReceiver,intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

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

            Location gdanska = new Location("gdanska");
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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trasy,menu);
        return true;

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.refresh_road:
                new GetData().execute();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public int time_eskadrowa_most(String tmp) {
        tmp = tmp.substring(11, 13);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }

    public int time_eskadrowa_trasa(String tmp) {

        tmp = tmp.substring(29, 31);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);


    }

    public int time_os_reda(String tmp) {
        tmp = tmp.substring(9, 11);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }

    public int time_struga_most_dlugi(String tmp) {
        tmp = tmp.substring(63, 66);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }

    public int time_struga_trasa_zamkowa(String tmp) {
        tmp = tmp.substring(32, 35);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }
    public int time_gdanska_most(String tmp) {
        tmp = tmp.substring(10, 14);
        tmp = tmp.replaceAll("[^0-9]", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }


    public int time_szosa_os_reda(String tmp) {
        tmp = tmp.substring(9, 12);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        tmp = tmp.trim();

        return Integer.parseInt(tmp);

    }

    public int time_szosa_most_dlugi(String tmp) {
        tmp = tmp.substring(63, 67);
        tmp = tmp.replaceAll("[^0-9]", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);

    }

    public int time_szosa_trasa_zamkowa(String tmp) {
        tmp = tmp.substring(32, 35);
        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);
    }
    public String time_brama_portowa(String tmp) {
        tmp = tmp.substring(23, 26);
        tmp = tmp.trim();
        return tmp;
    }

    public int parsing_brama_portowa(String tmp) {

        tmp = tmp.replaceAll("'", "");
        tmp = tmp.replaceAll(" ", "");
        return Integer.parseInt(tmp);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public  class GetData extends AsyncTask<String,String,String>
    {

        private String tmp_brama_portowa_text, tmp_eskadrowa_text, tmp_gdanska_txt, tmp_szosa_txt;
        private String tmp_struga_text;

        @Override
        protected String doInBackground(String... strings) {


            try {
                certificate_authentication();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(https_url);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                print_content(con);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            preparing_data();

            eskadrowa_text = tmp_eskadrowa_text;
            gdanska_txt = tmp_gdanska_txt;
            szosa_txt = tmp_szosa_txt;
            struga_text = tmp_struga_text;
            brama_portowa_text = tmp_brama_portowa_text;

            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(),"Pobieram dane",Toast.LENGTH_SHORT).show();
        }

        public void preparing_data() {

            tmp_brama_portowa_text = trim_Brama(tmp_brama_portowa_text);
            tmp_gdanska_txt = trim_gdanska(tmp_gdanska_txt);
            tmp_eskadrowa_text = trim_Eskadrowa(tmp_eskadrowa_text);
            tmp_struga_text = trim_struga(tmp_struga_text);
            tmp_szosa_txt = trim_szosa(tmp_szosa_txt);

            Log.d("z",tmp_struga_text);

        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),"Pobrano dane",Toast.LENGTH_SHORT).show();
        }

        private void certificate_authentication() throws KeyManagementException, NoSuchAlgorithmException {

            SSLContext  ctx = SSLContext.getInstance("TLS");

            ctx.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

        }

        private void print_content(HttpsURLConnection con) {
            if (con != null) {


                try {

                    //
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String input;
                    int lines = 0;

                    Pattern pattern = Pattern.compile("<value>AUTOSTRADA A6");
                    Pattern pattern1 = Pattern.compile("<value>MOST D");
                    Pattern pattern2 = Pattern.compile("<value>Os. ");


                    while ((input = br.readLine()) != null) {

                        lines++;

                        Matcher matcher_brama = pattern.matcher(input);
                        Matcher matcher_eskadrowa = pattern1.matcher(input);
                        Matcher matcher_szosa_stargardzka = pattern2.matcher(input);

                        if (matcher_brama.find()) {
                            tmp_brama_portowa_text = input;
                        }
                        if (matcher_eskadrowa.find()) {
                            tmp_eskadrowa_text = input;
                        }
                        if (matcher_szosa_stargardzka.find()) {
                            tmp_struga_text = input;
                        }
                        if (lines == 79) {
                            tmp_szosa_txt = input;
                        }
                        if (lines == 196) {
                            tmp_gdanska_txt = input;
                        }

                    }
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public String trim_Brama(String tmp) {
            tmp = tmp.trim();
            return tmp.substring(7, 33);
        }

        public String trim_Eskadrowa(String tmp) {
            tmp = tmp.trim();
            tmp = tmp.substring(7, 40);
            return tmp.replace("D�?UGI", "DŁUGI");
        }



        public String trim_struga(String tmp) {
            tmp = tmp.substring(27, 95);
            return tmp.replace("D�?UGI", "DŁUGI");
        }



        public String trim_gdanska(String tmp) {
            tmp = tmp.trim();
            tmp = tmp.substring(7, 40);
            return tmp.replace("D�?UGI", "DŁUGI");
        }



        public String trim_szosa(String tmp) {
            tmp = tmp.substring(27, 95);
            return tmp.replace("D�?UGI", "DŁUGI");
        }


    }


}