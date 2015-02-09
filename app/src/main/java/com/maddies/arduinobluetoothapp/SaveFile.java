package com.maddies.arduinobluetoothapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;


public class SaveFile extends AsyncTask<Object, String, Void> {

    @Override
    protected Void doInBackground(Object... params) {
        byte[] arduinoFileByteArray = (byte[]) params[0];
        String nameGetFile = (String) params[1];


        File directory = new File(Environment.getExternalStorageDirectory(), "Communico");
        if (!directory.mkdirs()) {
            Log.e(MainActivity.TAG, "Directory not created");
        } else {
            File file = new File(directory, nameGetFile);
            try {
                FileOutputStream fileOutputStream =new FileOutputStream(file.getAbsolutePath());

                fileOutputStream.write(arduinoFileByteArray[0]);
                fileOutputStream.close();
                Log.d(MainActivity.TAG, "File saved");
            }
            catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }

        }
        return null;
    }
}
