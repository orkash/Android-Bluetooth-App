package com.maddies.arduinobluetoothapp;

// imports all the packages that we need
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    
    public static int state = 1;
    public static final boolean DEBUG_MODE = true;
    public static final String TAG = "Bluetooth App";
    public BluetoothAdapter mBluetoothAdapter;
    public ArrayAdapter<String> bluetoothDeviceArrayAdapter;
    public static ConnectThread bluetoothThread;
    Spinner bluetoothSpinner;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        // restores previously stored preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // sets the layout
        setContentView(R.layout.activity_main);        

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDeviceArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line);
        bluetoothSpinner = (Spinner) findViewById(R.id.spinner);

        // Registers BraodcastReceiver for bluetooth state changes
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);

        // Regsiters BroadcastReceiver for new bluetooth devices
        IntentFilter bluetoothDeviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, bluetoothDeviceFilter);

        // When the search button is clicked it will search for Bluetooth devices
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if the device has bluetooth
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth and forces app shutdown
                    closeApplication("Your device doesn't support Bluetooth.");


                } else {
                    // Device does have bluetooth
                    // Checks if bluetooth is already enabled
                    if (!mBluetoothAdapter.isEnabled()) {
                        // bluetooth is not enabled
                        // user gets option to enable bluetooth
                        enableBluetooth();

                    } else {
                        // bluetooth is enabled
                        // device start searching for devices
                        if (mBluetoothAdapter.startDiscovery()){
                            Log.d(TAG, "you have started searching");
                        }
                    }
                }


            }
        });

        // When the select file button is clicked open the file explorer
        Button selectFileButton = (Button) findViewById(R.id.select_file_button);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (bluetoothThread.connectedThread.isAlive()) {
                    // there is a connection
                    // open the explorer
                    Intent intent = new Intent(MainActivity.this, FileExplore.class);
                    startActivity(intent);
                } else {
                    // there is no connection
                    // display message that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Before selecting a file you need to be connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // When the connect button clicked, a connection will be tried to make
       Button connectButton = (Button) findViewById(R.id.connect_button);
       connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askMakeConnection("", bluetoothSpinner.getSelectedItem().toString());
            }
        });

    }

    // this function will try to enable bluetooth
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    // this is called after the user decide the enable bluetooth or not
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                // User succesfully enabled bluetooth
                // device will start searching
                if (mBluetoothAdapter.startDiscovery()){
                    Log.d(TAG, "you have started searching");
                }
            }
            if (resultCode == RESULT_CANCELED) {
                // User stoped the bluetooth enabling process
                // the application will close
                closeApplication("The application works only with Bluetooth enabled. ");
            }
        }
    }


    // this broadcast receiver listens for new bluetooth devices and for bluetooth state changes
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // a new device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d(TAG, "new device found");

                // gets the mac address and device name chosen in the preferences
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                String prefBluetoothMacAddress = sharedPreferences.getString("pref_bluetooth_mac_address", "");
                String prefBluetoothDeviceName = sharedPreferences.getString("pref_bluetooth_device_name", "");
                
                
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in the spinner
                bluetoothDeviceArrayAdapter.add(device.getName() + " " + device.getAddress());

                String stringDevice = device.getName() + " " + device.getAddress();

                // if the new device has same mac address as in preferences, start a connection
                if (device.getAddress().equals(prefBluetoothMacAddress)) {
                    askMakeConnection("A device matched your preferred MAC Address.", stringDevice);
                    Log.d(TAG, "connecting because of Mac address");
                                       
                }

                // if the new device has same device name as in preferences, start a connection
                if (device.getName().equals(prefBluetoothDeviceName)) {

                    askMakeConnection("A device matched your preferred Name.", stringDevice);
                    Log.d(TAG, "connecting because of Device name");

                }

                // the spinner will be updated to show the new device
                bluetoothSpinner.setAdapter(bluetoothDeviceArrayAdapter);
            }

            // the bluetooth state has changed
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


    // this will be called if the user decided to make a connection
    // asks if the user is sure
    private void askMakeConnection(final String message, final String device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message + " Do you want to connect to: " + device +  "?")
                .setCancelable(true)
                .setTitle("Make Connection?")
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        bluetoothThread = new ConnectThread(device);
                        bluetoothThread.start();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    // will close the application
    public void closeApplication(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message + " The application will now close.")
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

    //
    @Override
    public void onDestroy() {
        // closes the broadcast receiver to release system resources
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
        // Handle action bar item clicks here.
        int id = item.getItemId();

        switch (id){
            // developer button is pressed
            case R.id.action_bar_developers:
                // show a message to user
                Toast.makeText(getApplicationContext(),
                        "Developed by Matthijs & Maarten", Toast.LENGTH_SHORT).show();

                return true;
            // settings button is pressed
            case R.id.action_bar_settings:
                // opens the preferences activity
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
