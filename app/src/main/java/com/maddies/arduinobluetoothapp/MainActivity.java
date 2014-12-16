package com.maddies.arduinobluetoothapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends Activity {

    public static final String TAG = "Bluetooth App";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    Spinner spinner;
    ArrayAdapter<String> mArrayAdapter;
    Button connectButton;

    public static ConnectThread bluetoothThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line);
        spinner =(Spinner) findViewById(R.id.spinner);

        // sets up broadcast receiver for bluetooth state
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);


        // Register the BroadcastReceiver
        IntentFilter bluetoothDeviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, bluetoothDeviceFilter);


        // searches for arduino devices
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // does device have bluetooth

                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    closeApplication("Your device doesn't support Bluetooth.");

                } else {
                    // Device supports bluetooth

                    if (!mBluetoothAdapter.isEnabled()) {
                        // bluetooth is not enabled
                        enableBluetooth();

                    } else {
                        // bluetooth is enabled
                        lookForArduino();
                    }
                }


            }
        });

        // madgijs button, does nothign except crash, very stupid don't try
        Button makeError = (Button) findViewById(R.id.error_button);
        /*makeError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                bluetoothThread = new ConnectThread();
                bluetoothThread.start();
            }
        });*/


        // open the file explorer
       /* Button selectFileButton = (Button) findViewById(R.id.select_file_button);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothThread.connectedThread.isAlive()) {
                    Intent intent = new Intent(MainActivity.this, FileExplore.class);
                    startActivity(intent);
                }
            }
        });*/

        connectButton = (Button) findViewById(R.id.search_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothThread = new ConnectThread();
                bluetoothThread.start();
            }
        });

    }



    // tries to find an arduino
    private void lookForArduino() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // have you connected to another bluetooth device once?
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() == "TP-SPEAKER" || device.getAddress() == "TP-SPEAKER" ) {
                    // you have already met this arduino
                    Toast.makeText(getApplicationContext(), "Welcome back", Toast.LENGTH_SHORT).show();
                }
            }

        }

        if (mBluetoothAdapter.startDiscovery()){
            Log.d(TAG, "you have started searching");
        };
    }


    // Gets result after enabling bluetooth
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                // User succesfully enabled bluetooth
                lookForArduino();
            }
            if (resultCode == RESULT_CANCELED) {
                // User stops the bluetooth enabling process
                closeApplication("The application works only with Bluetooth enabled");
            }
        }
    }

    // enables bluetooth
    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }



    // listens if user manually disables bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d("TAG", "new Device yeahhhh");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                if (device.getAddress() == "what is the adrress?") {
                    mBluetoothAdapter.cancelDiscovery();
                    connectButton.setVisibility(View.VISIBLE);
                }

                spinner.setAdapter(mArrayAdapter);
            }


            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "User turned Bluetooth off");

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Bluetooth was disabled. Do you want to enable it?")
                                .setCancelable(false)
                                .setTitle("Error")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        enableBluetooth();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        closeApplication("The application works only with Bluetooth enabled");
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, " User is turning bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "User switched bluetooth on");
                        mBluetoothAdapter.cancelDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "User is turning bluetooth on");
                        break;
                    default:
                        Log.d(TAG, "what happened?");
                }
            }
        }
    };

    // will exit application but not close if user has bluetooth problems
    private void closeApplication(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message + " The application will no close.")
                .setCancelable(false)
                .setTitle("Error")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onDestroy() {
        // closes the broadcast receiver
        unregisterReceiver(mReceiver);
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            // developer button is pressed
            case R.id.developers:
                Toast toast = Toast.makeText(getApplicationContext(), "By Matthijs & Maarten", Toast.LENGTH_SHORT);
                toast.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
