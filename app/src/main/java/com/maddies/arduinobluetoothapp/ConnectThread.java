package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

// this class is for establishing a connection between the arduino and android device
class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    public ConnectedThread connectedThread;

    // get called when the user accepted to make a connection
    public ConnectThread(String deviceNameAndAddress) {
        BluetoothSocket tmp;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // it will run through all bonded devices on your devices and see if they have the same mac adress and
        // device name. If they do, then that bonded device will be selected.
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                String nameAndAddress = device.getName() + " " + device.getAddress();

                Log.d(MainActivity.TAG, "loop: " +nameAndAddress);
                Log.d(MainActivity.TAG, "trying to find: " + deviceNameAndAddress);

                if (nameAndAddress.equals(deviceNameAndAddress))
                    mmDevice = device;
                    Log.d(MainActivity.TAG, "Found the bonded device with same address and name");
            }
        }

        // sees if our selected bluetooth device has the correct UUID
        // this UUID is unique for our arduino
        // so basically checks if our selected bluetooth device is an arduino from us
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket = tmp;
        } catch (IOException e) {

        }
    }

    public void run() {
        // device will stop searching for other devices
        mBluetoothAdapter.cancelDiscovery();

        try {
            // will try to make a connection
            mmSocket.connect();
            Log.d(MainActivity.TAG, "is connected");
        } catch (IOException connectException) {
            try {
                // error in trying to make a connection
                mmSocket.close();
            } catch (IOException closeException) {
            }
            return;
        }

        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
        Log.d(MainActivity.TAG, "establishing connection thread");

    }
}
