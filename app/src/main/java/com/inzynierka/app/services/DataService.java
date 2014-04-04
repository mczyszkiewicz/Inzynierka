package com.inzynierka.app.services;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

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
    protected String brama_service, eskadrowa_service, gdanska_service, szosa_service, struga_service;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new GetData().execute();
        return START_STICKY;


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();

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
            Toast.makeText(getBaseContext(), "Pobieram dane", Toast.LENGTH_SHORT).show();
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

            eskadrowa_service = strings.get(0);
            gdanska_service = strings.get(1);
            szosa_service = strings.get(2);
            struga_service = strings.get(3);
            brama_service = strings.get(4);
            Intent intent = new Intent("data");
            intent.putExtra("eskadrowa",eskadrowa_service);
            intent.putExtra("gdanska",gdanska_service);
            intent.putExtra("szosa",szosa_service);
            intent.putExtra("struga",struga_service);
            intent.putExtra("brama",brama_service);

            Toast.makeText(getBaseContext(), "Pobrano dane", Toast.LENGTH_SHORT).show();
            sendBroadcast(intent);
         //   Toast.makeText(getBaseContext(), brama_service, Toast.LENGTH_SHORT).show();

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



