package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// this class is for the actual data transmission between the two devices when they are already connected
class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    Handler handler;
    Context context;

    public ConnectedThread(BluetoothSocket socket, Context context, Handler handler) {

        this.handler = handler;
        this.context = context;

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
        if (bytes == null) {
            Log.e(MainActivity.TAG, "ERROR: Sending empty bytes");
            return;
        }

        try {
            mmOutStream.write(bytes);

        } catch (IOException e) {
            Log.e(MainActivity.TAG, "ERROR: IOException while sending bytes");
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

      //  if (/*you got the array of items*/  true) {
      //      openDialog();
      //  }
    }

    private void openDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, PostGetActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                String[] sampleArray = {"LOL", "YO"};
                i.putExtra(MainActivity.EXTRA_FILES, sampleArray);
                context.startActivity(i);
            }
        });
    }


    public void cancel() {
        try {
            mmSocket.close();
            // go to mainactivity
        } catch (IOException e) {
        }
    }
}
