package com.lego.minddroid.vegleges.BTConnection;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.UUID;

public class BTConnectAsyncTask extends AsyncTask<Void, Void, String> {

    public interface TranslateCompleteListener {
        public void onTaskComplete(String aText);
    }

    private Context context = null;
    private ProgressDialog progressDialog = null;
    private Handler handlerStatus;
    private BluetoothDevice device;
    private String selectedUUID;

    public BTConnectAsyncTask(Context context, Handler aHandlerStatus,
                              BluetoothDevice aDevice, String aSelectedUUID)
    {
        this.context = context;
        handlerStatus = aHandlerStatus;
        device = aDevice;
        selectedUUID = aSelectedUUID;
        progressDialog = new ProgressDialog(this.context);
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog.setMessage("Connecting...");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            UUID serviceUuid = UUID.fromString(selectedUUID);
            BTCommunicator.getInstance().connect(device, serviceUuid, handlerStatus);
            return "OK";
        } catch (Exception e) {
            return ("Error: "+e.getMessage());
        }
    }


    @Override
    protected void onPostExecute(String result)
    {
        progressDialog.dismiss();
    }
}
