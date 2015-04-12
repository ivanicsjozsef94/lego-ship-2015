package com.lego.minddroid.vegleges.BTConnection;

import com.lego.minddroid.vegleges.R;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> devices;

    public DeviceListAdapter() {
        devices = new ArrayList<BluetoothDevice>();
    }

    public void addDevice(BluetoothDevice device) {
        boolean exists = false;

        for(BluetoothDevice existingDevice : devices) {
            if(existingDevice.getAddress().equals(device.getAddress()))
                exists = true;
        }

        if(!exists)
            devices.add(device);
    }

    public int getCount() {
        return devices.size();
    }

    public Object getItem(int position) {
        return devices.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.devlice_list_item, null);
        ((TextView)view.findViewById(R.id.devicelist_text1)).setText(devices.get(position).getName());
        ((TextView)view.findViewById(R.id.devicelist_text2)).setText(devices.get(position).getAddress());
        return view;
    }

}