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
            read();
        }
    }


    public void write(byte[] bytes) {
        try {
            Log.e("Banana", "Byte Array Length: " + bytes.length);
            for (int i = 0; i < bytes.length; i += 64) {
                for (int j = i * 64; j < 64 * (i + 1); j++) {
                    if (j > bytes.length - 1)
                        break;
                    mmOutStream.write(bytes[j]);
                    Log.e("Output", "" + bytes[j]);
                }
            }
        } catch (IOException e) {
            Log.e("Oh oh", "ERROR");
        }
        Log.e("Banana", "FILE SENT");
    }

    byte[] b = new byte[64];

    public byte[] read() {
        try {
            mmInStream.read(b);
        } catch (IOException e) {
        }
        Log.e("Banana", "FILE RECEIVED");
        for (byte bite : b) {
            Log.e("Banana", "" + bite);
        }
        return b;
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}