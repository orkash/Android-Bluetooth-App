package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    public ConnectedThread connectedThread;


    public ConnectThread() {
        BluetoothSocket tmp;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                if (device.getName().equals("HC-06")) ;
                mmDevice = device;
            }
        }


        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket = tmp;
        } catch (IOException e) {
        }
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
            }
            return;
        }

        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
    }
}
