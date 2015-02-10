package com.maddies.arduinobluetoothapp;

// imports all the packages that we need

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity {

    public static int state = 1;
    public static int protocolState = 0;

    private static final int REQUEST_ENABLE_BT = 1;
    public final static String TAG = "Bluetooth App";
    public final static String EXTRA_DEVICE = "DEVICE";
    public final static String EXTRA_ENABLE_BLUETOOTH = "ENABLE_BLUETOOTH";
    public static final String EXTRA_FILES = "FILES";
    public final static byte[] ASK_FILES_BYTES = {3};
    public final static String PUT_ARRAY = "ARRAY";

    public final static int FAILED_CONNECTING = 10;
    public final static int SUCCESS_CONNECTING = 20;
    public final static int GOT_DATA = 20;

    public static BluetoothAdapter mBluetoothAdapter;
    public static ConnectThread connectThread;

    @InjectView(R.id.devices_list_view)
    ListView devicesListView;
    ArrayList<BluetoothDevice> devicesArrayList;
    BluetoothDevicesAdapter mBluetoothDevicesAdapter;

    @InjectView(R.id.connecting_panel)
    ProgressBarCircularIndeterminate connectingProgressBar;
    @InjectView(R.id.loading_panel)
    ProgressBarCircularIndeterminate loadingProgressBar;
    @InjectView(R.id.search_button)
    ButtonRectangle searchButton;
    @InjectView(R.id.stop_button)
    ButtonRectangle stopButton;

    String prefBluetoothMacAddress, prefBluetoothName, prefBluetoothUUID;

    BluetoothDevice selectedDevice;

    IntentFilter intentFilter;

    SharedPreferences sharedPreferences;

    boolean bluetoothAvailible;

    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FAILED_CONNECTING:
                    connectingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.making_connection_failed), Toast.LENGTH_SHORT).show();
                    return;
                case SUCCESS_CONNECTING:
                    connectingProgressBar.setVisibility(View.GONE);
                    Intent startPostGet = new Intent(MainActivity.this, PostGetActivity.class);
                    startPostGet.putExtra(MainActivity.EXTRA_DEVICE, selectedDevice);
                    MainActivity.this.startActivity(startPostGet);
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        // restores previously stored preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // sets the layout
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devicesArrayList = new ArrayList<>();
        mBluetoothDevicesAdapter = new BluetoothDevicesAdapter(this, devicesArrayList);
        mBluetoothDevicesAdapter.setNotifyOnChange(true);

        // Attach the adapter to a ListView
        devicesListView.setAdapter(mBluetoothDevicesAdapter);

        // check if the device has bluetooth
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth and forces app shutdown
            bluetoothAvailible = false;
        } else {
            bluetoothAvailible = true;
        }

        // on first run
        sharedPreferences = getSharedPreferences("preferences.xml", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("firstRunMainActivity", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            displayShowCaseView(this, getString(R.string.tutorial_search_button_header),
                    getString(R.string.tutorial_search_button_text), R.id.search_button);
            sharedPreferences.edit().putBoolean("firstRunMainActivity", false).apply();
        }


        // When the search button is clicked it will search for Bluetooth devices
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAvailible) {
                    // Device does have bluetooth
                    // Checks if bluetooth is already enabled
                    if (!mBluetoothAdapter.isEnabled()) {
                        // bluetooth is not enabled
                        enableBluetooth();

                    } else {
                        // bluetooth is enabled
                        startBluetooth();
                    }

                } else {

                    closeApplication(getString(R.string.error_no_bluetooth));
                }
            }
        });

        // selects a device and initiates connection
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                askMakeConnection("", devicesArrayList.get(position));
            }
        });

        // device will stop searching for new devices
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAvailible) {
                    mBluetoothAdapter.cancelDiscovery();
                    loadingProgressBar.setVisibility(View.GONE);


                } else {
                    closeApplication(getString(R.string.error_no_bluetooth));
                }
            }
        });
    }

    public static void displayShowCaseView(final Context context, String title, String message, final int id) {
        ShowcaseView.Builder showcaseViewBuilder = new ShowcaseView.Builder((Activity) context)
                .setContentTitle(title)
                .setContentText(message)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        showcaseView.setVisibility(View.GONE);

                        if (id == R.id.search_button) {
                            displayShowCaseView(context, context.getString(R.string.tutorial_stop_button_header),
                                    context.getString(R.string.tutorial_stop_button_text),
                                    R.id.stop_button);

                        } else if (id == R.id.stop_button) {
                            displayShowCaseView(context, context.getString(R.string.tutorial_list_view_header),
                                    context.getString(R.string.tutorial_list_view_text),
                                    R.id.devices_list_view);

                        } else if (id == R.id.devices_list_view) {
                            return;
                        }


                        if (id == R.id.post_button) {
                            displayShowCaseView(context, context.getString(R.string.tutorial_get_button_header),
                                    context.getString(R.string.tutorial_get_button_text),
                                    R.id.get_button);
                        } else if (id == R.id.get_button) {
                            displayShowCaseView(context, context.getString(R.string.tutorial_cancel_button_header),
                                    context.getString(R.string.tutorial_cancel_button_text),
                                    R.id.cancel_button);
                        } else if (id == R.id.cancel_button) {
                            displayShowCaseView(context, context.getString(R.string.tutorial_status_text_view_header),
                                    context.getString(R.string.tutorial_status_text_view_text),
                                    R.id.status_text_view);

                        } else if (id == R.id.status_text_view) {
                            // nothings needs to be done if the cancel button tutorial
                        }
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }
                });

        if (id == R.id.devices_list_view || id == R.id.status_text_view) {
            // no overflow, so last showcase view
            showcaseViewBuilder.setStyle(R.style.CustomShowcaseThemeClose);
        } else {
            // the last image
            showcaseViewBuilder.setStyle(R.style.CustomShowcaseThemeNext);
        }

        showcaseViewBuilder.setTarget(new ViewTarget(id, (Activity) context));

        showcaseViewBuilder.build();
    }

    public void makeToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void startBluetooth() {
        if (mBluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "you have started searching");
            loadingProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                }
            });

        }
    }


    // Gets result after enabling bluetooth
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // User succesfully enabled bluetooth
                startBluetooth();
            }
            if (resultCode == RESULT_CANCELED) {
                // User stops the bluetooth enabling process
                closeApplication(getString(R.string.error_bluetooth_not_enabled));
            }
        }
    }

    // enables bluetooth
    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getExtras().getString(TAG).equals(EXTRA_ENABLE_BLUETOOTH)) {
            enableBluetooth();
        }
    }


    // listens if user manually disables bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (devicesArrayList.contains(device)) {
                    return;
                }

                devicesArrayList.add(device);
                devicesListView.setAdapter(mBluetoothDevicesAdapter);

                retreivePreferences();

                Log.d(TAG, "name: " + device.getName() + "-- address : " + device.getAddress());

                if (device.getAddress().equals(prefBluetoothMacAddress)) {
                    askMakeConnection(getString(R.string.connection_dialog_preferred_mac), device);
                    Log.d(TAG, "connecting because of Mac address");

                } else if (device.getName().equals(prefBluetoothName)) {
                    askMakeConnection(getString(R.string.connection_dialog_preferred_name), device);
                    Log.d(TAG, "connecting because of Device name");

                }
            }

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int intentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (intentState) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "User turned Bluetooth off");

                        new MaterialDialog.Builder(MainActivity.this)
                                .content(getString(R.string.bluetooth_disabled_message))
                                .negativeColor(Color.GRAY)
                                .cancelable(false)
                                .title(getString(R.string.error))
                                .icon(getResources().getDrawable(R.drawable.ic_alert))
                                .positiveText(getString(R.string.yes))
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        enableBluetooth();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                        closeApplication(getString(R.string.error_bluetooth_not_enabled));
                                    }
                                })
                                .negativeText(getString(R.string.no))
                                .show();

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, " User is turning bluetooth off...");
                        mBluetoothAdapter.cancelDiscovery();
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

    public void retreivePreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        prefBluetoothMacAddress = sharedPreferences.getString("pref_bluetooth_mac_address", "");
        prefBluetoothName = sharedPreferences.getString("pref_bluetooth_device_name", "");
        prefBluetoothUUID = sharedPreferences.getString("pref_bluetooth_uuid", "");
        Log.d(TAG, prefBluetoothUUID);
    }

    private void askMakeConnection(String message, final BluetoothDevice device) {
        String stringDevice = device.getName() + " " + device.getAddress();
        new MaterialDialog.Builder(MainActivity.this)
                .content(message + getString(R.string.connection_dialog_message) + stringDevice + "?")
                .cancelable(true)
                .negativeColor(Color.GRAY)
                .title(getString(R.string.connection_dialog_title))
                .positiveText(getString(R.string.connection_dialog_connect))
                .negativeText(getString(R.string.connection_dialog_cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        selectedDevice = device;
                        retreivePreferences();

                        connectThread = new ConnectThread(handler, device, getApplicationContext());
                        connectThread.start();

                        loadingProgressBar.setVisibility(View.GONE);
                        connectingProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();


    }


    // will exit application but not close if user has bluetooth problems
    private void closeApplication(String message) {
        new MaterialDialog.Builder(MainActivity.this)
                .content(message)
                .cancelable(false)
                .title(getString(R.string.error))
                .icon(getResources().getDrawable(R.drawable.ic_alert))
                .positiveText(getString(R.string.ok))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        loadingProgressBar.setVisibility(View.GONE);
                        connectingProgressBar.setVisibility(View.GONE);
                    }
                })
                .show();
    }


    @Override
    protected void onStop() {
        // closes the broadcast receiver
        unregisterReceiver(mReceiver);
        super.onStop();

    }

    @Override
    protected void onStart() {
        // Registers BroadcastReceiver for bluetooth status changes
        registerReceiver(mReceiver, intentFilter);
        super.onStart();
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

        switch (id) {
            // developer button is pressed
            case R.id.action_bar_developers:
                makeToast(MainActivity.this, getString(R.string.developers));
                return true;
            case R.id.action_bar_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
