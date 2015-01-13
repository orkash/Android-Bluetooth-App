package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

// this class is for the actual data transmission between the two devices when they are already connected
class ConnectedThread extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();

            tmpOut = socket.getOutputStream();

        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        while (true) {
            readState();
        }
    }

    static int counter = 0;

    public void write(byte[] bytes) {
        if (bytes == null) {
            Log.d(MainActivity.TAG, "Sent Empty File " + counter);
            counter++;
            return;
        }

        try {
            Log.i(MainActivity.TAG, "Working");
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("Oh oh", "ERROR");
        }

    }

    public void readState() {
        try {
            MainActivity.state = mmInStream.read();
            Log.d(MainActivity.TAG, "Reading " + MainActivity.state);

            if (MainActivity.state == -1)
                MainActivity.state = 1;
        } catch (IOException e) {

        }
    }

    public void cancel() {
        try {
            mmSocket.close();
            // go to mainactivity
        } catch (IOException e) {
        }
    }
}
