/*
package com.maddies.arduinobluetoothapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;


class SaveFile extends AsyncTask<Object, String, Void> {

    @Override
    protected Void doInBackground(Object... params) {
        byte[] arduinoFileByteArray = (byte[]) params[0];
        String nameGetFile = (String) params[1];

        Log.i(MainActivity.TAG, "" + arduinoFileByteArray.length);
        for(byte b : arduinoFileByteArray){
            Log.i(MainActivity.TAG, "" + b);
        }

        File directory = new File(Environment.getExternalStorageDirectory(), "Communico");
        directory.mkdirs();

        File file = new File(directory, nameGetFile);
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());

            fileOutputStream.write(arduinoFileByteArray);
            fileOutputStream.close();

            PostGetActivity.statusTextView.setText("File Saved");

            Log.i(MainActivity.TAG, "File saved");
        } catch (java.io.IOException e) {
            Log.e(MainActivity.TAG, "Exception in photoCallback", e);
        }

        return null;
    }

}
*/
