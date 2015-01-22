package com.maddies.arduinobluetoothapp;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostGetActivity extends Activity {


    static TextView connectedTo, statusTextView;
    BluetoothDevice device;
    Button getButton, postButton, cancelButton;
    SharedPreferences sharedPreferences;


    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<>();
    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;
    private static final String TAG = "F_PATH";
    private Item[] fileList;
    private File path = Environment.getExternalStorageDirectory();
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_get);

        connectedTo = (TextView) findViewById(R.id.connected_to_text_view);
        statusTextView = (TextView) findViewById(R.id.status_text_view);

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
        postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                /*if (MainActivity.connectThread.connectedThread.isAlive()) {
                    // there is a connection
                    // open the explorer

                }*/
                if (isExternalStorageReadable()) {
                    loadFileList();

                    showDialog(DIALOG_LOAD_FILE);
                    Log.d(TAG, path.getAbsolutePath());
                }
            }
        });


        getButton = (Button) findViewById(R.id.get_button);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (ConnectThread.connectedThread.isAlive() && isExternalStorageWritable()) {
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
        // Registers BraodcastReceiver for bluetooth state changes
        IntentFilter bluetoothStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, bluetoothStateFilter);

        super.onStart();
    }

    private void loadFileList() {
        if (path.mkdirs() || path.isDirectory()) {
            Log.e(TAG, "able to write to sd card");
        } else {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                } else {
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }

        adapter = new ArrayAdapter<Item>(this,
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (fileList == null) {
            Log.e(TAG, "No files loaded");
            dialog = builder.create();
            return dialog;
        }

        builder.setTitle(getString(R.string.file_explorer_dialog_title));
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chosenFile = fileList[which].file;
                File sel = new File(path + "/" + chosenFile);
                if (sel.isDirectory()) {
                    firstLvl = false;

                    // Adds chosen directory to list
                    str.add(chosenFile);
                    fileList = null;
                    path = new File(sel + "");

                    loadFileList();

                    removeDialog(DIALOG_LOAD_FILE);
                    showDialog(DIALOG_LOAD_FILE);
                    Log.d(TAG, path.getAbsolutePath());

                }

                // Checks if 'up' was clicked
                else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                    // present directory removed from list
                    String s = str.remove(str.size() - 1);

                    // path modified to exclude present directory
                    path = new File(path.toString().substring(0,
                            path.toString().lastIndexOf(s)));
                    fileList = null;

                    // if there are no more directories in the list, then
                    // its the first level
                    if (str.isEmpty()) {
                        firstLvl = true;
                    }
                    loadFileList();

                    removeDialog(DIALOG_LOAD_FILE);
                    showDialog(DIALOG_LOAD_FILE);
                    Log.d(TAG, path.getAbsolutePath());

                }
                // File picked
                else {
                    try {
                        sendFile(sel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        dialog = builder.show();
        return dialog;
    }


    public static void sendFile(File file) throws IOException {
        fileSent = false;
        tempState = 0;

        PostGetActivity.statusTextView.setText("sending");

        while (!fileSent){
            MainActivity.connectThread.connectedThread.write(readFile(file));
        }

        statusTextView.setText("Done");

    }

    static int tempState = 0;
    static boolean fileSent = false;

    private static byte[] readFile(File file) throws IOException {
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
