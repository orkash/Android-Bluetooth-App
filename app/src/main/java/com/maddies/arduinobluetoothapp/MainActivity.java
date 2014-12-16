package com.maddies.arduinobluetoothapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final String TAG = "Bluetooth App";

    private static final int REQUEST_ENABLE_BT = 1;
    public static ConnectThread bluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sets up broadcast receiver for bluetooth state
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);


        // searches for arduino devices
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // does device have bluetooth
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth

                    closeAplication("Your device doesn't support Bluetooth.");

                } else {
                    // Device supports bluetooth

                    if (!mBluetoothAdapter.isEnabled()) {
                        // bluetooth is not enabled

                        enableBluetooth();
                        Log.d(TAG, "Enabling Bluetooth");

                    } else {
                        // bluetooth is enabled
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                }


            }
        });

        // madgijs button, does nothign except crash
        Button makeError = (Button) findViewById(R.id.error_button);
        makeError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                bluetoothThread = new ConnectThread();
                bluetoothThread.start();
            }
        });


        // open the file explorer
        Button selectFileButton = (Button) findViewById(R.id.select_file_button);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothThread.connectedThread.isAlive()) {
                    Intent intent = new Intent(MainActivity.this, FileExplore.class);
                    startActivity(intent);
                }
            }
        });
    }




    // Gets result after enabling bluetooth
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                // User succesfully enabled bluetooth
                Log.d(TAG, "User successfully enabled bluetooth");
                lookForArduino();
            }
            if (resultCode == RESULT_CANCELED) {
                // User stops the bluetooth enabling process
                Log.d(TAG, "User stops the bluetooth enabling process");

                closeAplication("The application works only with Bluetooth enabled");
            }
        }
    }


    // tries to find an arduino
    private void lookForArduino() {

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
            final String action = intent.getAction();

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
                                        closeAplication("The application works only with Bluetooth enabled");
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
    private void closeAplication(String message) {
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
