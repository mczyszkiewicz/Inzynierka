package com.inzynierka.app.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.inzynierka.app.R;

/*
 * Created by Mateusz Czyszkiewicz.
 */
public class GPSLocation extends Service implements LocationListener {

    private final Context context;
    private boolean canGetLocation = false;
    private static final long MIN_DISTANCE = 20;
    private static final long MIN_TIME = 10000;
    private Location location;
    private double latitude;
    private double longitude;
    public LocationManager locationManager;

    public GPSLocation(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            boolean isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isINTERNETenabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSenabled && !isINTERNETenabled) {
                this.canGetLocation = true;
                {
                    if (isINTERNETenabled) {
                        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                latitude = getLatitude();
                                longitude = getLongitude();
                            }
                        }
                    }
                    if (isGPSenabled) {
                        if (location == null) {
                            if (location == null) {
                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                                if (locationManager != null) {
                                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (location != null) {
                                        latitude = getLatitude();
                                        longitude = getLongitude();

                                    }

                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.brak_polaczenia), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return location;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    public double getLatitude() {
        if (location != null)
            latitude = location.getLatitude();
        return latitude;
    }

    public double getLongitude() {
        if (location != null)
            longitude = location.getLongitude();
        return longitude;
    }

    @Override
    public void onLocationChanged(Location location) {
   }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

        Toast.makeText(context,"jest gps",Toast.LENGTH_SHORT).show();
          }

    @Override
    public void onProviderDisabled(String s) {

          }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




}
