package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

// this class is for establishing a connection between the arduino and android device
class ConnectThread extends Thread {

    Handler handler;
    Context context;

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public static ConnectedThread connectedThread;

    // get called when the user accepted to make a connection
    public ConnectThread(Handler handler, BluetoothDevice device, Context context) {

        this.handler = handler;
        this.context = context;

        BluetoothSocket tmp = null;
        mmDevice = device;

        connectedThread = null;

        Log.d(MainActivity.TAG, "connecting to: " + device.getName() + " - " + device.getAddress() );

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // this uuid is the also used by the server code

            String s = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_bluetooth_uuid", "");
            Log.d(MainActivity.TAG, s);
            UUID uuid = UUID.fromString(s);
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);

        } catch (IOException e) {
        }

        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        MainActivity.mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            Log.d(MainActivity.TAG, "is connected");

        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.d(MainActivity.TAG, "can't connect ");

            handler.obtainMessage(MainActivity.FAILED_CONNECTING).sendToTarget();

           /*
                    Just for Testing
           */
            handler.obtainMessage(MainActivity.SUCCESS_CONNECTING).sendToTarget();

            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // connection succesfully made
        // go to post get activity
        connectedThread = new ConnectedThread(mmSocket, context, handler);
        connectedThread.start();


        handler.obtainMessage(MainActivity.SUCCESS_CONNECTING).sendToTarget();

        Log.d(MainActivity.TAG, "establishing connection thread");
    }

    public void cancel() {
        try {
            mmSocket.close();

        } catch (IOException e) { }
    }
}


