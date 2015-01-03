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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static int state = 1;
    public static final boolean DEBUG_MODE = true;
    private static final int REQUEST_ENABLE_BT = 1;

    public static final String TAG = "Bluetooth App";

    public static BluetoothAdapter mBluetoothAdapter;
    public ArrayAdapter<String> bluetoothDeviceArrayAdapter;
    public static ConnectThread connectThread;
    ListView bluetoothListView;
    public List<BluetoothDevice> bluetoothList = new ArrayList<>();

    static ProgressBar connectingPanel, loadingPanel;
    Button searchButton, stopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restores previously stored preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // sets the layout
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        loadingPanel = (ProgressBar) findViewById(R.id.loading_panel);
        connectingPanel = (ProgressBar) findViewById(R.id.connecting_panel);

        searchButton = (Button) findViewById(R.id.search_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        bluetoothDeviceArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line);
        bluetoothListView = (ListView) findViewById(R.id.list_view);



        // Registers BraodcastReceiver for bluetooth state changes
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);

        // Registers BroadcastReceiver for new bluetooth devices
        IntentFilter bluetoothDeviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, bluetoothDeviceFilter);

        // When the search button is clicked it will search for Bluetooth devices
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
                        enableBluetooth();

                    } else {
                        // bluetooth is enabled
                        startBluetooth();
                    }
                }
            }
        });

        // selects a device and initiates connection
        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    askMakeConnection("", String.valueOf(adapterView.getItemAtPosition(position)),
                            bluetoothList.get(position));
                } catch (NullPointerException e) {
                    makeToast("There are no bluetooth devices");
                }
            }

        });

        // device will stop searching for new devices

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.cancelDiscovery();
                loadingPanel.setVisibility(View.GONE);
            }
        });
    }

    private void makeToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void startBluetooth() {
        if (mBluetoothAdapter.startDiscovery()){
            Log.d(TAG, "you have started searching");
            loadingPanel.setVisibility(View.VISIBLE);
        };
    }


    // Gets result after enabling bluetooth
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                // User succesfully enabled bluetooth
                startBluetooth();
            }
            if (resultCode == RESULT_CANCELED) {
                // User stops the bluetooth enabling process
                closeApplication("The application works only with Bluetooth enabled. ");
            }
        }
    }

    // enables bluetooth
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }



    // listens if user manually disables bluetooth
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Log.d(TAG, "new device found");

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                String prefBluetoothMacAddress = sharedPreferences.getString("pref_bluetooth_mac_address", "");
                String prefBluetoothDeviceName = sharedPreferences.getString("pref_bluetooth_device_name", "");


                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                bluetoothDeviceArrayAdapter.add(device.getName() + " " + device.getAddress());

                // add device to separate list
                bluetoothList.add(device);

                String stringDevice = device.getName() + " " + device.getAddress();


                Log.d(TAG, "name: " + device.getName() + "-- address : " + device.getAddress());
                Log.d(TAG, "PREFERENCES -- name: " + prefBluetoothDeviceName + "-- address : " + prefBluetoothMacAddress);

                if (device.getAddress().equals(prefBluetoothMacAddress)) {
                    askMakeConnection("A device matched your preferred MAC Address.", stringDevice, device);
                    Log.d(TAG, "connecting because of Mac address");

                } else if (device.getName().equals(prefBluetoothDeviceName)) {
                    askMakeConnection("A device matched your preferred Name.", stringDevice, device);
                    Log.d(TAG, "connecting because of Device name");

                }

                bluetoothListView.setAdapter(bluetoothDeviceArrayAdapter);
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
                                .setIcon(R.drawable.ic_alert)
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

    private void askMakeConnection(String message, final String deviceString, final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message + " Do you want to connect to: " + deviceString +  "?")
                .setCancelable(true)
                .setTitle("Make Connection?")
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        connectThread = new ConnectThread(MainActivity.this ,device);
                        connectThread.start();
                        loadingPanel.setVisibility(View.GONE);
                        connectingPanel.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // no action required
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }


    // will exit application but not close if user has bluetooth problems
    public void closeApplication(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message + " The application will now close.")
                .setCancelable(false)
                .setTitle("Error")
                .setIcon(R.drawable.ic_alert)
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
            case R.id.action_bar_developers:
                makeToast("Developed by Matthijs & Maarten");
                return true;
            case R.id.action_bar_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


}
