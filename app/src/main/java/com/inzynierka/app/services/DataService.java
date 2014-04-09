package com.inzynierka.app.services;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.inzynierka.app.R;
import com.inzynierka.app.gps.GPSLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DataService extends Service {

    private static final String https_url = "https://szr.szczecin.pl/utms/data/layers/VMSPublic";
    private static final double brama_latitude = 14.54829;
    private static final double brama_longitude = 53.4249;
    private static final double gdanska_longitude = 53.41528;
    private static final double gdanska_latitude = 14.56944;
    private static final double szosa_longitude = 53.3726;
    private static final double szosa_latitude = 14.70708;
    private static final double struga_longitude = 53.3845;
    private static final double struga_latitude = 14.65139;
    private static final double eskadrowa_longitude = 53.3897;
    private static final double eskadrowa_latitude = 14.62139;

    GPSLocation gps = new GPSLocation(DataService.this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new GetData().execute();
        if (gps.canGetLocation()) {
            LocationCreator(gps.getLatitude(), gps.getLongitude());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    public void LocationCreator(double Mylatitude, double Mylongitude) {

        Location myLocation = new Location(getString(R.string.MyLocation));
        myLocation.setLatitude(Mylatitude);
        myLocation.setLongitude(Mylongitude);

        Location brama = CreateLocation(brama_latitude, brama_longitude, getString(R.string.brama));
        Location gdanska = CreateLocation(gdanska_latitude, gdanska_longitude, getString(R.string.gdanska));
        Location szosa = CreateLocation(szosa_latitude, szosa_longitude, getString(R.string.szosa));
        Location eskadrowa = CreateLocation(eskadrowa_latitude, eskadrowa_longitude, getString(R.string.eskadrowa));
        Location struga = CreateLocation(struga_latitude, struga_longitude, getString(R.string.struga));

        float distance_to_brama = myLocation.distanceTo(brama);
        float distance_to_gdanska = myLocation.distanceTo(gdanska);
        float distance_to_szosa = myLocation.distanceTo(szosa);
        float distance_to_eskadrowa = myLocation.distanceTo(eskadrowa);
        float distance_to_struga = myLocation.distanceTo(struga);

        Intent i = new Intent(getString(R.string.Location_update));

        if (distance_to_brama < 300) {
            i.putExtra(getString(R.string.message), getString(R.string.tab_brama));
        } else if (distance_to_gdanska < 300) {
            i.putExtra(getString(R.string.message), getString(R.string.tab_gdanska));
        } else if (distance_to_szosa < 300) {
            i.putExtra(getString(R.string.message), getString(R.string.tab_szosa));
        } else if (distance_to_eskadrowa < 300) {
            i.putExtra(getString(R.string.message), getString(R.string.tab_eskadrowa));
        } else if (distance_to_struga < 300) {
            i.putExtra(getString(R.string.message), getString(R.string.tab_stuga));
        }

        sendBroadcast(i);
    }

    public Location CreateLocation(double latitude, double longitude, String name) {

        Location loc = new Location(name);
        loc.setLongitude(longitude);
        loc.setLatitude(latitude);

        return loc;
    }

    public class GetData extends AsyncTask<String, String, List<String>> {

        private String tmp_brama_portowa_text, tmp_eskadrowa_text, tmp_gdanska_txt, tmp_szosa_txt;
        private String tmp_struga_text;
        @Override
        protected List<String> doInBackground(String... strings) {

            List<String> lista = new ArrayList<String>();
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

            lista.add(tmp_eskadrowa_text);
            lista.add(tmp_gdanska_txt);
            lista.add(tmp_szosa_txt);
            lista.add(tmp_struga_text);
            lista.add(tmp_brama_portowa_text);

            return lista;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(), getString(R.string.pobieram_dane), Toast.LENGTH_SHORT).show();
        }

        public void preparing_data() {

            tmp_brama_portowa_text = trim_Brama(tmp_brama_portowa_text);
            tmp_gdanska_txt = trim_gdanska(tmp_gdanska_txt);
            tmp_eskadrowa_text = trim_Eskadrowa(tmp_eskadrowa_text);
            tmp_struga_text = trim_struga(tmp_struga_text);
            tmp_szosa_txt = trim_szosa(tmp_szosa_txt);
        }

        @Override
        protected void onPostExecute(List<String> strings) {

            Intent intent = new Intent(getString(R.string.data));
            intent.putExtra(getString(R.string.eskadrowa),strings.get(0));
            intent.putExtra(getString(R.string.gdanska),strings.get(1));
            intent.putExtra(getString(R.string.szosa),strings.get(2));
            intent.putExtra(getString(R.string.struga),strings.get(3));
            intent.putExtra(getString(R.string.brama),strings.get(4));

            Toast.makeText(getBaseContext(), getString(R.string.pobrano_dane), Toast.LENGTH_SHORT).show();
            sendBroadcast(intent);
        }

        private void certificate_authentication() throws KeyManagementException, NoSuchAlgorithmException {


            SSLContext ctx = SSLContext.getInstance("TLS");

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


                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String input;
                    int lines = 0;

                    Pattern pattern = Pattern.compile(getString(R.string.pattern1));
                    Pattern pattern1 = Pattern.compile(getString(R.string.pattern2));
                    Pattern pattern2 = Pattern.compile(getString(R.string.pattern3));


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
            return tmp.replace(getString(R.string.dlugi), getString(R.string.Długi));
        }


        public String trim_struga(String tmp) {
            tmp = tmp.substring(27, 95);

            return tmp.replace((getString(R.string.dlugi)), getString(R.string.Długi));
        }


        public String trim_gdanska(String tmp) {
            tmp = tmp.trim();
            tmp = tmp.substring(7, 40);
            return tmp.replace((getString(R.string.dlugi)), getString(R.string.Długi));
        }


        public String trim_szosa(String tmp) {
            tmp = tmp.substring(27, 95);

            return tmp.replace((getString(R.string.dlugi)), getString(R.string.Długi));
        }


    }

}



