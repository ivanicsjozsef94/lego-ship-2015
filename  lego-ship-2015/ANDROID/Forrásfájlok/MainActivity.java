package com.lego.minddroid.vegleges;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lego.minddroid.vegleges.BTConnection.BTCommunicator;
import com.lego.minddroid.vegleges.BTConnection.BTConnectAsyncTask;
import com.lego.minddroid.vegleges.BTConnection.LCPMessage;
import com.lego.minddroid.vegleges.Location.CompassTracker;
import com.lego.minddroid.vegleges.Location.GPSTracker;
import com.lego.minddroid.vegleges.Network.NetworkConnection;
import com.lego.minddroid.vegleges.Network.TrackingConnection;

import org.w3c.dom.Text;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    private static int REQUEST_ENABLE_BT = 100;
    TextView connectedBT;
    TextView connectedNXT;
    TextView networkEnabled;
    static TextView batteryLevel;
    TextView coordinateX;
    TextView coordinateY;
    TextView compassTextView;
    TextView gpsTextView;
    ImageView compassView;
    TextView tempCoordinateX;
    TextView tempCoordinateY;
    TextView finalCoordinateX;
    TextView finalCoordinateY;
    static TextView motorPercentagesA;
    static TextView motorPercentagesB;
    static TextView motorPercentagesC;
    static MediaPlayer mediaPlayer;
    static TextView reached;


    BluetoothDevice fineDevice;
    WifiManager wifiManager;
    LocationManager locationManager;

    static GPSTracker gpsTracker;
    static CompassTracker compassTracker;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case BTCommunicator.DATA_RECEIVED:
                    byte[] readBuf = (byte[]) msg.obj;
                    //Toast.makeText(MainActivity.this, readBuf.length, Toast.LENGTH_LONG).show();
                    getBatteryLevel(readBuf);
                    break;
                case BTCommunicator.CONNECTION_SUCCESSFULL:
                    BTCommunicator.getInstance().write(LCPMessage.getBeepMessage());
                    connectionOpened();
                    break;
                case BTCommunicator.CONNECTION_FAILED:
                    Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    } catch (Exception ex) {}
                    BTCommunicator.getInstance().cancel();
                    connectToDevice();
                    break;
                case BTCommunicator.CONNECTION_CLOSED:
                    connectionClosed();
                    BTCommunicator.getInstance().cancel();
                    connectToDevice();
                    break;
            }
        }
    };
    public void connectionClosed() {
        Toast.makeText(MainActivity.this, "Connection closed", Toast.LENGTH_LONG).show();

        connectedNXT.setText("Disconnected!");
        connectedNXT.setTextColor(Color.RED);
    }
    public void connectionOpened() {
        Toast.makeText(MainActivity.this, "Successful connection", Toast.LENGTH_LONG).show();
        BTCommunicator.getInstance().start();
        connectedNXT.setText(fineDevice.getName());
        connectedNXT.setTextColor(Color.GREEN);
        BTCommunicator.getInstance().write(LCPMessage.getBatteryInfo());
    }
    static int batteryLevelInMillis = 0;
    public static void getBatteryLevel(byte[] bytes) {
        int val1 = bytes[5];
        int val2 = (bytes[6]<<8);
        //Log.i("MainActivity:", val1 + "/" + val2);
        double sum = ((((double)val2+(double)val1-5000)/4000)*100);
        batteryLevelInMillis = val2+val1;
        sum = Math.round(sum);
        batteryLevel.setText((int)sum + "%");
        if(sum>=70) {
            batteryLevel.setTextColor(Color.GREEN);
        } else if(sum >= 30) {
            batteryLevel.setTextColor(Color.rgb(255,153,51));
        } else {
            batteryLevel.setTextColor(Color.RED);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectedBT = (TextView) findViewById(R.id.tv_bluetooth);
        connectedNXT= (TextView) findViewById(R.id.tv_nxt);
        networkEnabled=(TextView)findViewById(R.id.tv_network);
        coordinateX = (TextView) findViewById(R.id.tv_coordinates_x);
        coordinateY = (TextView) findViewById(R.id.tv_coordinates_y);
        tempCoordinateX=(TextView)findViewById(R.id.tv_temp_coord_x);
        tempCoordinateY=(TextView)findViewById(R.id.tv_temp_coord_y);
        finalCoordinateX=(TextView)findViewById(R.id.tv_final_coord_x);
        finalCoordinateY=(TextView)findViewById(R.id.tv_final_coord_y);
        compassView = (ImageView)findViewById(R.id.compass_image);
        compassTextView=(TextView) findViewById(R.id.tv_compass);
        gpsTextView = (TextView) findViewById(R.id.tv_gps);
        batteryLevel= (TextView) findViewById(R.id.tv_battery_level);
        motorPercentagesA = (TextView) findViewById(R.id.motor_percentages_a);
        motorPercentagesB = (TextView) findViewById(R.id.motor_percentages_b);
        motorPercentagesC = (TextView) findViewById(R.id.motor_percentages_c);
        reached = (TextView) findViewById(R.id.reach);

        gpsTracker = new GPSTracker(MainActivity.this, coordinateX, coordinateY);
        compassTracker = new CompassTracker(compassView, compassTextView, MainActivity.this);
        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.s_o);

    }
    @Override
    public void onStart() {
        super.onStart();
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsTextView.setText("Disabled");
            gpsTextView.setTextColor(Color.RED);
            GPSAlertDialog();
        } else {
            gpsTextView.setText("Enabled");
            gpsTextView.setTextColor(Color.GREEN);
            setGPSenabled(true);
        }
        if(!wifiManager.isWifiEnabled()) {
            networkEnabled.setText("Disabled");
            networkEnabled.setTextColor(Color.RED);
            WifiAlertDialog();
        } else {
            networkEnabled.setText("Enabled");
            networkEnabled.setTextColor(Color.GREEN);
        }

        tempCoordinateX.setText("0");
        tempCoordinateY.setText("0");
        finalCoordinateX.setText("0");
        finalCoordinateY.setText("0");
    }

    public void setGPSenabled(boolean enabled) {
        if(enabled) {
            gpsTracker.locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    gpsTracker); //Önmaga kezelje a listenert!
        } else {
            gpsTracker.locationManager.removeUpdates(gpsTracker);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //statikus példány - ha nincs bekapcsolva, akkor bekapcsoljuk
        if(!BTCommunicator.getInstance().getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //Ha sikeresen bekapcsolta, akkor kiíírjuk, hogy enabled, különben
        //disabled
        if(BTCommunicator.getInstance().getBluetoothAdapter().isEnabled()) {
            TextView tvBluetooth = (TextView) findViewById(R.id.tv_bluetooth);
            tvBluetooth.setText("Enabled");
            tvBluetooth.setTextColor(Color.GREEN);
        } else {
            TextView tvBluetooth = (TextView) findViewById(R.id.tv_bluetooth);
            tvBluetooth.setText("Disabled");
            tvBluetooth.setTextColor(Color.RED);
        }
        toCallAsynchronous();
    }

    private void WifiAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Turns on Network!");
        alertDialog.setMessage("If Wifi and 3G is disabled, then we can't connect" +
                "to the server!");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void GPSAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Turns on GPS!");
        alertDialog.setMessage("GPS is disabled. We can't get any location data " +
                "if the GPS and Network are offline! Please, turns on GPS!");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onPause() {
        BTCommunicator.getInstance().cancel();
        setGPSenabled(false);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void allConnection(View v) {
        connectToDevice();
        setGPSenabled(true);
    }
    public void connectToDevice() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String btAddress = sharedPreferences.getString(DeviceFounder.KEYBTADDRESS, "");
        String selectedUUID = sharedPreferences.getString(DeviceFounder.KEYUUID, "");

        Set<BluetoothDevice> pairedDevices = BTCommunicator.getInstance().getBluetoothAdapter().getBondedDevices();
        boolean deviceFound = false;
        for(BluetoothDevice device : pairedDevices) {
            if(device.getAddress().equals(btAddress)) {
                new BTConnectAsyncTask(
                         this, handler, device, selectedUUID).execute();
                deviceFound = true;
                fineDevice = device;
            }
        }


        if(!deviceFound) {
            Toast.makeText(this, "Device is not paired: " + btAddress, Toast.LENGTH_LONG).show();
        }
    }
    static int counter1 = 0;
    public void toCallAsynchronous() {
        final Handler handler = new Handler();
        Timer timer = new Timer();

        final Driving driving = new Driving(
                Double.parseDouble(tempCoordinateX.getText().toString()),
                Double.parseDouble(tempCoordinateY.getText().toString()),
                Double.parseDouble(finalCoordinateX.getText().toString()),
                Double.parseDouble(finalCoordinateY.getText().toString()));

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if(counter1 == 3) {
                                NetworkConnection m = new NetworkConnection(
                                        tempCoordinateX,
                                        tempCoordinateY,
                                        finalCoordinateX,
                                        finalCoordinateY);
                                m.execute();
                            }
                        } catch (Exception e) { Log.e("MainActivity", e.getMessage()); }
                        if(counter1 == 3) {
                            counter1 = 0;
                            String compass = ""+Math.abs(CompassTracker.getCurrentDegree());
                            doTrackingConnection(compass);
                        } else { counter1++; }
                        try {
                            Driving.changeCoordinates(
                                    Double.parseDouble(tempCoordinateX.getText().toString()),
                                    Double.parseDouble(tempCoordinateY.getText().toString()),
                                    Double.parseDouble(finalCoordinateX.getText().toString()),
                                    Double.parseDouble(finalCoordinateY.getText().toString()));
                            driving.automatedControlling(
                                    Double.parseDouble(coordinateX.getText().toString()),
                                    Double.parseDouble(coordinateY.getText().toString()));
                        } catch (Exception e) {
                            Log.d("MainActivity: ", "There is no GPS connection yet!");
                            MainActivity.loggingString("MainActivity: " + "There is no GPS connection yet!");
                        }
                    }});
                }
            };
            timer.schedule(doAsynchronousTask, 0, 1000); // execute in every second

    }

    public void doTrackingConnection(String compass) {
        TrackingConnection n = new TrackingConnection();
        n.execute(
                coordinateX.getText().toString(),
                coordinateY.getText().toString(),
                ""+Driving.getSpeed(),
                String.valueOf((double)batteryLevelInMillis/1000),
                compass,
                String.valueOf(Driving.getDistance()),
                logString);
        logString = "";
        Log.i("MainActivity", ""+CompassTracker.getCurrentDegree());
    }
    static void changeMotorPercentagesTextViews(String a, String b, String c) {
        motorPercentagesA.setText(a);
        motorPercentagesB.setText(b);
        motorPercentagesC.setText(c);
    }

    public void showDeviceFounder(View v) {
        startActivity(new Intent(this, DeviceFounder.class));
    }

    private static String logString = "";
    public static void loggingString(String tag) {
            logString += tag;
            logString += "\n";
    }

    public static int manualUp = 0;
    public static int manualLeft = 0;
    public static int manualRight = 0;


    public static void setManualForward(int value) {
        manualUp = value;
    }
    public static void setManualLeft(int value) {
        manualLeft = value;
    }
    public static void setManualRight(int value) {
        manualRight = value;
    }
}
