package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
            Log.i(MainActivity.TAG, "Length: " + bytes.length);

            for (int i = 0; i < bytes.length; i++) {
                if (i % 400 == 0)
                    Log.i(MainActivity.TAG, "Writing byte");

                mmOutStream.write(bytes[i]);

            }

        } catch (IOException e) {
            Log.e(MainActivity.TAG, "ERROR: IOException while sending bytes");
        }

    }

    public void readState() {
        try {
            if (mmInStream.available() > 0) {
                if (MainActivity.protocolState == 1) {
                    try {
                        MainActivity.state = mmInStream.read();
                        Log.d(MainActivity.TAG, "Reading " + MainActivity.state);

                        if (MainActivity.state == -1)
                            MainActivity.state = 1;
                    } catch (IOException e) {

                    }
                } else if (MainActivity.protocolState == 2) {

                    ArrayList<Byte> incomingBytes = new ArrayList<>();

                    while (mmInStream.available() > 0) {
                        incomingBytes.add((byte) mmInStream.read());

                        if (mmInStream.available() == 0) {
                            for (int i = 0; i < 2000; i++) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (mmInStream.available() > 0)
                                    break;
                            }
                        }

                    }

                    saveFile(convertToPrimitiveByteArray(incomingBytes));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] convertToPrimitiveByteArray(ArrayList<Byte> in) {
        final int n = in.size();
        byte[] out = new byte[in.size()];
        for (int i = 0; i < n; i++) {
            out[i] = in.get(i);
        }
        return out;
    }

    private void saveFile(final byte[] file) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, PostGetActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra(MainActivity.EXTRA_FILES, file);
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
