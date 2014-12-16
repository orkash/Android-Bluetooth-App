package com.maddies.arduinobluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
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

    public static final String LOG = "Bluetooth App";

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Your device has no bluetooth")
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onDestroy();
                            }
                        });
                    
                    AlertDialog alert = builder.create();
                    alert.show();
                    
                } else {
                    // Device supports bluetooth
                    Log.i(LOG, "Wow you have bluetooth");


                    if (!mBluetoothAdapter.isEnabled()) {
                        // bluetooth is not enabled
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        Log.i(LOG, "Enabling Bluetooth");

                    } else {
                        Log.i(LOG, "Bluetooth is enabled");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                // User succesfully enabled bluetooth
//              String result=data.getStringExtra("result");
                Log.i(LOG, "User succesfully enabled bluetooth");
            }
            if (resultCode == RESULT_CANCELED) {
                // User stops the bluetooth enabling process
                Log.i(LOG, "User stops the bluetooth enabling process");
            }
        }
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
                        Log.i(LOG, "User turned Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(LOG, " User is turning bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(LOG, "User switched bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(LOG, "User is turning bluetooth on");
                        break;
                    default:
                        Log.i(LOG, "what happened?");
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        // closes the broadcast receiver
        unregisterReceiver(mReceiver);

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
