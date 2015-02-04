package com.maddies.arduinobluetoothapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PostGetActivity extends ActionBarActivity
        implements ArduinoFileDialogFragment.ArduinoFileDialogListener, AndroidFileDialogFragment.AndroidFileDialogListener {

    @InjectView(R.id.connected_to_text_view)TextView connectedTo;
    @InjectView(R.id.status_text_view) TextView statusTextView;

    @InjectView(R.id.get_button) Button getButton;
    @InjectView(R.id.post_button) Button postButton;
    @InjectView(R.id.cancel_button) Button cancelButton;
    @InjectView(R.id.progressBar) ProgressBar progressBar;

    SharedPreferences sharedPreferences;
    BluetoothDevice device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_get);

        ButterKnife.inject(this);

        device = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);
        connectedTo.setText(getString(R.string.connected_to) + device.getName() + " - " + device.getAddress());


        // on first run
        sharedPreferences = getSharedPreferences("com.maddies.arduinobluetoothapp", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("firstRunPostGetActivity", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            MainActivity.displayShowCaseView(this, getString(R.string.tutorial_post_button_header),
                    getString(R.string.tutorial_post_button_text),
                    R.id.post_button);
            sharedPreferences.edit().putBoolean("firstRunPostGetActivity", false).apply();
        }

        // When the select file button is open the file explorer and send it
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                /*if (MainActivity.connectThread.connectedThread.isAlive()) {
                    // there is a connection
                    // open the explorer

                }*/

                if (isExternalStorageReadable()) {
                    DialogFragment dialog = new AndroidFileDialogFragment();
                    dialog.show(getSupportFragmentManager(), MainActivity.TAG + "AndroidFileDialogFragment");
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Could not read the SD card", Toast.LENGTH_SHORT).show();
                }

            }
        });


        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino

                /*ConnectThread.connectedThread.write(MainActivity.ASK_FILES_BYTES);*/
                statusTextView.setText("Asking for files list");
                progressBar.setVisibility(View.VISIBLE);
                String[] lol = {"lol", "sup"};
                openArduinoFilePicker(lol);

                /*if (isExternalStorageWritable()) {
                    DialogFragment dialog = new ArduinoFileDialogFragment();
                    dialog.show(getSupportFragmentManager(), MainActivity.TAG + "ArduinoFileDialogFragment");

                } else {
                    // there is no connection
                    // display message to user that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Could not open the file explorer", Toast.LENGTH_SHORT).show();
                }*/

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the current connection
                // not tested yet
                ConnectThread.connectedThread.cancel();
                // go back to mainactivity
                finish();
            }
        });
    }


    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(MainActivity.TAG, "User turned Bluetooth off");

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(MainActivity.TAG, " User is turning bluetooth off...");

                        AlertDialog.Builder builder = new AlertDialog.Builder(PostGetActivity.this);
                        builder.setMessage(getString(R.string.bluetooth_disabled_message))
                                .setCancelable(false)
                                .setTitle(R.string.error)
                                .setIcon(R.drawable.ic_alert)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // close the connection
                                        Intent intent = new Intent(PostGetActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra(MainActivity.TAG, MainActivity.EXTRA_ENABLE_BLUETOOTH);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        closeApplication(getString(R.string.error_bluetooth_not_enabled));
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(MainActivity.TAG, "User switched bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(MainActivity.TAG, "User is turning bluetooth on");
                        break;
                    default:
                        Log.d(MainActivity.TAG, "what happened?");
                }
            }
        }
    };

    public void closeApplication(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostGetActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(R.string.error)
                .setIcon(R.drawable.ic_alert)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(PostGetActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onStop() {
        // closes the broadcast receiver
        unregisterReceiver(mReceiver);
        super.onStop();

    }

    @Override
    public void onStart() {
        // closes the broadcast receiver
        // Registers BroadcastReceiver for bluetooth state changes
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);

        super.onStart();
    }

    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    ----------------------------------------------------------------------
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


    public void openArduinoFilePicker(String[] array) {
        progressBar.setVisibility(View.INVISIBLE);

        if (isExternalStorageWritable()) {
            DialogFragment dialog = ArduinoFileDialogFragment.newInstance(array);
            dialog.show(getSupportFragmentManager(), MainActivity.TAG + "ArduinoFileDialogFragment");

        } else {
            // there is no connection
            // display message to user that there is no connection
            Toast.makeText(getApplicationContext(),
                    "Could not open the file explorer", Toast.LENGTH_SHORT).show();
        }

    }


    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the ArduinoFileDialogFragment.ArduinoFileDialogListener interface
    @Override
    public void onAndroidFileClick(DialogFragment dialog, File file, Boolean again) {
        // user chose an item
        if (again) {
            // chose a directory
            dialog.dismiss();
            dialog.show(getSupportFragmentManager(), MainActivity.TAG + "AndroidFileDialogFragment");
        } else {
            // chose a file

            statusTextView.setText("Sending File");
            progressBar.setVisibility(View.VISIBLE);

            /*try {
                sendFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onArduinoFileClick(DialogFragment dialog, int which) {
        // user chose an item
        Toast.makeText(getApplicationContext(), "You chose" + which, Toast.LENGTH_SHORT).show();
        statusTextView.setText("Getting File");
        progressBar.setVisibility(View.VISIBLE);

    }

    public void onCancel(DialogFragment dialog){
        statusTextView.setText("Cancelled");
    }


    public void sendFile(File file) throws IOException {
        fileSent = false;
        tempState = 0;

        statusTextView.setText("sending");

        while (!fileSent){
            MainActivity.connectThread.connectedThread.write(readFile(file));
        }

        statusTextView.setText("Done");

    }

    static int tempState = 0;
    static boolean fileSent = false;

    private byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            if (MainActivity.state == 1 && tempState == 0) {
                tempState = 1;
                Log.i(MainActivity.TAG, "Working1");

                return ByteBuffer.allocate(8).putLong(file.length()).array();
            } else if (MainActivity.state == 2 && tempState == 1) {
                tempState = 2;
                Log.i(MainActivity.TAG, "Working2");

                return file.getName().getBytes();
            } else if (MainActivity.state == 3 && tempState == 2) {
                tempState = 3;
                // Get and check length
                long longLength = f.length();
                int length = (int) longLength;
                if (length != longLength)
                    throw new IOException("File size >= 2 GB");
                Log.i(MainActivity.TAG, f.length() + "TEST");
                // Read file and return data
                byte[] data = new byte[length];
                f.readFully(data);
                fileSent = true;
                Log.i(MainActivity.TAG, "Working3");
                statusTextView.setText("Done");

                return data;
            }
            return null;
        } finally {
            f.close();
        }
    }

    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    ------------------------------------------------------------------------
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String[] arduinoFiles = intent.getStringArrayExtra(MainActivity.EXTRA_FILES);
        openArduinoFilePicker(arduinoFiles);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_get, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            // developer button is pressed
            case R.id.action_bar_developers:
                Toast toast = Toast.makeText(getApplicationContext(), R.string.developers, Toast.LENGTH_SHORT);
                toast.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
