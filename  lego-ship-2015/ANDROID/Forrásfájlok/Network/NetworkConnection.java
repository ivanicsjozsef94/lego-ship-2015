package com.lego.minddroid.vegleges.Network;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lego.minddroid.vegleges.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by József on 2015.03.11..
 */
public class NetworkConnection extends AsyncTask<Void, Void, String[]> {
    TextView tempCoordX = null;
    TextView tempCoordY = null;
    TextView finalCoordX = null;
    TextView finalCoordY = null;

    public NetworkConnection(
            TextView tempCoordX, TextView tempCoordY, TextView finalCoordX, TextView finalCoordY) {
        this.tempCoordX = tempCoordX;
        this.tempCoordY = tempCoordY;
        this.finalCoordX = finalCoordX;
        this.finalCoordY = finalCoordY;
    }

    private final String LOG_TAG = NetworkConnection.class.getSimpleName();

    private String[] getLocationDataFromJson(String networkJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String AMK_tCoordX = "tcoordx";
        final String AMK_tCoordY = "tcoordy";
        final String AMK_fCoordX = "fcoordx";
        final String AMK_fCoordY = "fcoordy";
        final String AMK_forward = "forward";
        final String AMK_left = "left";
        final String AMK_right = "right";
        Log.d("NetworkConnection:", networkJsonStr);
        JSONObject networkJson = new JSONObject(networkJsonStr);
        String tCoordX;
        String tCoordY;
        String fCoordX;
        String fCoordY;
        tCoordX = networkJson.getString(AMK_tCoordX);
        tCoordY = networkJson.getString(AMK_tCoordY);
        fCoordX = networkJson.getString(AMK_fCoordX);
        fCoordY = networkJson.getString(AMK_fCoordY);
        MainActivity.setManualForward(networkJson.getInt(AMK_forward));
        MainActivity.setManualRight(networkJson.getInt(AMK_right));
        MainActivity.setManualLeft(networkJson.getInt(AMK_left));


        String[] result = {
                tCoordX,
                tCoordY,
                fCoordX,
                fCoordY,
        };
        return result;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String networkJsonStr = null;

        String fromandroid = "1";

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String LEGO_BASE_URL =
                    "http://lego.amk.uni-obuda.hu/legogroup4/index.php?";
            final String ANDROID_PARAM = "fromandroid";

            Uri builtUri = Uri.parse(LEGO_BASE_URL).buildUpon()
                    .appendQueryParameter(ANDROID_PARAM, fromandroid)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.d("Network", builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
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
            networkJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            MainActivity.loggingString("NetworkConnection: Abortive network activity " +
                    "(failed to send data / input stream could not be created / " +
                    "couldn't received any data within a reasonable period of time!");
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    MainActivity.loggingString("NetworkConnection: Error closing stream!");
                }
            }
        }

        try {
            return getLocationDataFromJson(networkJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            MainActivity.loggingString("NetworkConnection: Uninterpretable datas from server!");
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if(result != null) {
            tempCoordX.setText(result[0]);
            tempCoordY.setText(result[1]);
            finalCoordX.setText(result[2]);
            finalCoordY.setText(result[3]);
        }
    }
}
