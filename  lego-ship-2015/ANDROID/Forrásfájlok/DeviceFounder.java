package com.lego.minddroid.vegleges;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.lego.minddroid.vegleges.BTConnection.BTCommunicator;
import com.lego.minddroid.vegleges.BTConnection.DeviceListAdapter;

import java.util.UUID;


public class DeviceFounder extends Activity {

    private DeviceListAdapter listAdapter;
    public static final String KEYBTADDRESS = "BTADDRESS"; //BT MAC-cím
    public static final String KEYUUID = "UUID";
    public static final String UUIDVALUE = "00001101-0000-1000-8000-00805F9B34FB";
    public static String KEYBTNAME = "BTNAME";

    private final BroadcastReceiver foundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Ha talált új eszközt, beteszi
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Le akarjuk tölteni az adatokat
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listAdapter.addDevice(device);
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_founder);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(foundReceiver, filter);

        BTCommunicator.getInstance().getBluetoothAdapter().startDiscovery();

        ListView listView = (ListView)findViewById(R.id.listView1);
        listAdapter = new DeviceListAdapter();
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                sendSelectedUUID(position);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(foundReceiver);
        BTCommunicator.getInstance().cancel();
    }

    private void sendSelectedUUID(final int aSelectPosition) {
        BluetoothDevice device = (BluetoothDevice) listAdapter.getItem(aSelectPosition);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEYBTADDRESS, device.getAddress());
        editor.putString(KEYUUID, UUIDVALUE);
        editor.putString(KEYBTNAME, device.getName());
        editor.commit();
        finish();
    }

}
