package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ConnectedThread extends Thread {
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


    public void write(byte[] bytes) {
        if (bytes == null)
            return;
        try {
            Log.i("TAG", "Working");
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("Oh oh", "ERROR");
        }

    }

    public void readState() {
        try {
            MainActivity.state = mmInStream.read();
            Log.i("TAG", "Reading " + MainActivity.state);

            if (MainActivity.state == -1)
                MainActivity.state = 1;
        } catch (IOException e) {

        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}
