package com.lego.minddroid.vegleges.Location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.lego.minddroid.vegleges.BTConnection.BTCommunicator;
import com.lego.minddroid.vegleges.BTConnection.LCPMessage;

/**
 * Created by József on 2015.02.12..
 */
public class CompassTracker extends Service implements SensorEventListener {

    private ImageView image;
    private Context context;

    // record the compass picture angle turned
    private static float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    public static float getCurrentDegree() {
        return currentDegree;
    }

    public CompassTracker(ImageView image, TextView textView, Context context) {

        // our compass image
        this.image = image;

        // TextView that will tell the user what degree is he heading
        tvHeading = textView;
        this.context = context;

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            BTCommunicator.getInstance().write(LCPMessage.getBatteryInfo());
        } catch (Exception e) {
            Log.e("CompassTracker", e.getMessage());
        }
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
