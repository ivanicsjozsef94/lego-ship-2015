package com.lego.minddroid.vegleges.Location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.lego.minddroid.vegleges.BTConnection.LCPMessage;
import com.lego.minddroid.vegleges.MainActivity;

/**
 * Created by József on 2015.02.11..
 */
public class GPSTracker extends Service implements LocationListener {

    private final static String LOG_TAG = GPSTracker.class.getSimpleName();
    private Context context;
    private TextView coordinateX;
    private TextView coordinateY;

    private boolean canGetLocation = false;

    Location location;
    public LocationManager locationManager;
    double latitude;
    double longitude;

    private final static long MIN_DISTANCE_FOR_UPDATE_LOC = 0;
    private final static long MIN_TIME_BTW_UPDATES = 0;

    public GPSTracker(Context context, TextView coordinateX, TextView coordinateY) {
        this.context = context;
        this.coordinateX = coordinateX;
        coordinateX.setText("N/A");
        coordinateX.setTextColor(Color.RED);
        this.coordinateY = coordinateY;
        coordinateY.setText("N/A");
        coordinateY.setTextColor(Color.RED);

        locationManager = (LocationManager) this.context.getSystemService(LOCATION_SERVICE);

        if(locationManager == null) {
            Log.e(LOG_TAG, "Location Manager is empty!");
        } else {
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                canGetLocation = true;
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BTW_UPDATES,
                        MIN_DISTANCE_FOR_UPDATE_LOC,
                        this
                );
            }
        }
    }

    public boolean getLocationPossible() {
        return canGetLocation;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    private void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
        coordinateX.setText("" + latitude);
        coordinateX.setTextColor(Color.BLUE);
        coordinateY.setText(""+longitude);
        coordinateY.setTextColor(Color.BLUE);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(LOG_TAG, "GPS is enabled!");
            canGetLocation = true;
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_FOR_UPDATE_LOC,
                    this
            );
        } else {
            locationManager.removeUpdates(this);
            Log.i(LOG_TAG, "GPS is disabled!");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
