package com.lego.minddroid.vegleges.Network;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.lego.minddroid.vegleges.MainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by József on 2015.04.09..
 */
public class TrackingConnection extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = TrackingConnection.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {

        if(params.length < 5) {
            Log.e(LOG_TAG, "There is not enough input params! Sorry:(");
            if(params[0] == null || params[1] == null) {
                Log.e(LOG_TAG, "I can't receive any datas from the GPS!");
            }
        }
        HttpURLConnection trackingConnection = null;
        BufferedReader reader = null;

        String networkJsonStr = null;
        try {
            final String LEGO_TRACKING_URL =
                    "http://lego.amk.uni-obuda.hu/legogroup4/tracking.php?";
            final String LOCATION_X_PARAM = "ccoordx";
            final String LOCATION_Y_PARAM = "ccoordy";
            final String SPEED = "speed";
            final String VOLTAGE = "voltage";
            final String COMPASS = "compass";
            final String DISTANCE = "distance";
            final String LOG = "log";

            Uri trackingUri = Uri.parse(LEGO_TRACKING_URL).buildUpon()
                    .appendQueryParameter(LOCATION_X_PARAM, params[0].toString())
                    .appendQueryParameter(LOCATION_Y_PARAM, params[1].toString())
                    .appendQueryParameter(SPEED, params[2].toString())
                    .appendQueryParameter(VOLTAGE, params[3].toString())
                    .appendQueryParameter(COMPASS, params[4].toString())
                    .appendQueryParameter(DISTANCE, params[5].toString())
                    .appendQueryParameter(LOG, params[6].toString())
                    .build();
            //TRACKING!!!
            URL urlTracking = new URL(trackingUri.toString());
            Log.d("Network", urlTracking.toString());
            // Create the request to OpenWeatherMap, and open the connection
            trackingConnection = (HttpURLConnection) urlTracking.openConnection();
            trackingConnection.setRequestMethod("GET");
            trackingConnection.connect();
            // Read the input stream into a String
            InputStream inputStream = trackingConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
        } catch (Exception e) {
            Log.e("TrackingError!", e.getMessage());
            MainActivity.loggingString("TrackingError: " +  e.getMessage());
            return null;
        } finally {
            if(trackingConnection != null) {
                trackingConnection.disconnect();
            }
        }

        return null;
    }
}
