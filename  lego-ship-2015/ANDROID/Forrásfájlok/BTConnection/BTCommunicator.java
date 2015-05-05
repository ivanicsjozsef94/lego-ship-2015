package com.lego.minddroid.vegleges.BTConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.lego.minddroid.vegleges.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BTCommunicator extends Thread {
    public static final int DATA_RECEIVED = 0;
    public static final int CONNECTION_SUCCESSFULL = 1;
    public static final int CONNECTION_FAILED = 2;
    public static final int CONNECTION_CLOSED = 3;

    //Ez reprezentálja a fizikai BlueTooth adaptert. Ezt használva
    //tudod felfedni a többi eszközt MAC-kel és BlueToothServerSocket
    //segítségével figyelni a bejövő kommunikációt!
    private BluetoothAdapter bluetoothAdapter = null;
    //Ezen keresztül engedi a rendszer, hogy egy app kommunikáljon egy
    //másik appal. Input- és OutputStreamen keresztül.
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private Handler msgHandler = null;
    private static boolean enabled = false;
    public static  BluetoothSocket clientSocket = null;

    //statikus példány kérése
    private static BTCommunicator instance = null;

    public static BTCommunicator getInstance() {
        if(instance == null) {
            instance = new BTCommunicator();
        }
        return instance;
    }

    public static BluetoothSocket getClientSocket() {
        return clientSocket;
    }

    protected BTCommunicator() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void connect(BluetoothDevice device, UUID uuid, Handler
            handler)
    {
        bluetoothAdapter.cancelDiscovery();
        msgHandler = handler;
        enabled = true;

        try {
            clientSocket = device.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
            inStream = clientSocket.getInputStream();
            outStream = clientSocket.getOutputStream();
            handler.obtainMessage(CONNECTION_SUCCESSFULL,
                    "").sendToTarget();
        } catch (Exception e) {
            Log.e("BTCommunicator", e.getMessage());
            MainActivity.loggingString("BTCommunicator: Unable to connect to NXT!");
            try {
                BTCommunicator.getInstance().cancel();
                handler.obtainMessage(CONNECTION_FAILED, e.getMessage
                        ()).sendToTarget();
                //clientSocket.close();
            } catch (Exception closeException) { Log.e("BTCommunicator", e.getMessage()); }
        }
    }

    public void run() {
        byte[] buffer = new byte[1024];
        buffer[0] = 0x00;buffer[1] = 0x00;buffer[2] = 0x00;buffer[3] = 0x00;buffer[4] = 0x00;
        int bytes;

        while (enabled) {
            try {
                bytes = inStream.read(buffer);
                msgHandler.obtainMessage(DATA_RECEIVED, bytes, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                msgHandler.obtainMessage(CONNECTION_CLOSED,
                        "").sendToTarget();

                try {
                    clientSocket.close();
                } catch (IOException e2) { }
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            if(outStream != null) {
                outStream.write(bytes);
            }
        } catch (IOException e) {
            Log.e("BTCOMM", e.getMessage());
            MainActivity.loggingString("BTCOMM: Sending data to NXT was unsuccesfully.");
        }
    }

    public void cancel() {
        enabled = false;

        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();

        if (msgHandler != null)
            /*msgHandler.obtainMessage(CONNECTION_CLOSED,
                    "").sendToTarget();*/

        if (outStream != null) {
            try {
                outStream.close();
                outStream = null;
            } catch (Exception e) {
            }
        }

        if (inStream != null) {
            try {
                inStream.close();
                inStream = null;
            } catch (Exception e) {
            }
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientSocket = null;
            } catch (IOException e) {
            }
        }

        instance = null;
    }
}
