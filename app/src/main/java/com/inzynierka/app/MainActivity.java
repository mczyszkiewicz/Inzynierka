package com.inzynierka.app;



import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.inzynierka.app.activites.InfoActivity;
import com.inzynierka.app.activites.TrasyActivity;
import com.inzynierka.app.fragments.AlertFragment;


public class MainActivity extends FragmentActivity {


    boolean internet_connection = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button lista = (Button)findViewById(R.id.button_lista);
        Button info = (Button)findViewById(R.id.button_info);


        lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                internet_connection = check_internet_connection();

                if(internet_connection)
                {
                    Intent intent = new Intent(MainActivity.this, TrasyActivity.class);
                    startActivity(intent);
                }
                else
              {
                  FragmentManager fm = getSupportFragmentManager();
                  AlertFragment alertFragment = new AlertFragment();
                  alertFragment.show(fm,"TAG");
              }
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }




   private boolean check_internet_connection()
   {
       ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
       return networkInfo != null && networkInfo.isConnectedOrConnecting();
   }





}
