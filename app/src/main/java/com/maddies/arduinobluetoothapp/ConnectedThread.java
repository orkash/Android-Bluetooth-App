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

	int state = 1;
	
    public void write(byte[] bytes) {
        try 
        	{
            mmOutStream.write(bytes[j]);
            }
        } catch (IOException e) {
            Log.e("Oh oh", "ERROR");
        }
    }

    public byte[] read() {
        try {
            state = mmInStream.read();
            if(state == -1)
            	state = 0;
        } catch (IOException e) {
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
