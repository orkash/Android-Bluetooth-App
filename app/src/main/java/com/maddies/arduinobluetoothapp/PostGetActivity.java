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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PostGetActivity extends Activity {

    TextView connectedTo;
    BluetoothDevice device;
    Button getButton, postButton, cancelButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_get);

        connectedTo = (TextView) findViewById(R.id.connected_to_text_view);

        device = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);

        connectedTo.setText(getString(R.string.connected_to) + device.getName() + " - " + device.getAddress());

        // starts tutorial chain

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
        postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (MainActivity.connectThread.connectedThread.isAlive()) {
                    // there is a connection
                    // open the explorer
                    Intent intent = new Intent(PostGetActivity.this, FileExplore.class);
                    startActivity(intent);
                }
            }
        });


        getButton = (Button) findViewById(R.id.get_button);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (MainActivity.connectThread.connectedThread.isAlive()) {
                    // there is a connection
                    // send
                } else {
                    // there is no connection
                    // display message to user that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Before selecting a file you need to be connected", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
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
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
        // Registers BraodcastReceiver for bluetooth state changes
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);

        super.onStart();
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
