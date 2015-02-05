package com.maddies.arduinobluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PostGetActivity extends ActionBarActivity  {

    @InjectView(R.id.connected_to_text_view) TextView connectedTo;
    @InjectView(R.id.status_text_view) TextView statusTextView;

    @InjectView(R.id.get_button) ButtonRectangle getButton;
    @InjectView(R.id.post_button) ButtonRectangle postButton;
    @InjectView(R.id.cancel_button) ButtonRectangle cancelButton;
    @InjectView(R.id.progressBar)   ProgressBarCircularIndeterminate progressBar;

    SharedPreferences sharedPreferences;
    BluetoothDevice device;



    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<>();
    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private ArrayList<Item> fileList = new ArrayList<>();
    private File path = Environment.getExternalStorageDirectory();
    private String chosenFile;
    ListAdapter adapter;


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

                byte[] stateToBeSent = {2};
                MainActivity.connectThread.connectedThread.write(stateToBeSent);
                MainActivity.protocolState = 2;


                if (isExternalStorageReadable()) {
                    openAndroidFilePicker();

                    if (fileList == null) {
                        // no files loaded
                    }

                    /*DialogFragment dialog = new AndroidFileDialogFragment();
                    dialog.show(getSupportFragmentManager(), MainActivity.TAG + "AndroidFileDialogFragment");*/
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

                byte[] stateToBeSent = {3};
                MainActivity.connectThread.connectedThread.write(stateToBeSent);
                MainActivity.protocolState = 3;

                if (isExternalStorageWritable()) {

                    statusTextView.setText("Asking for files list");
                    progressBar.setVisibility(View.VISIBLE);

                    ArrayList<Item> list = new ArrayList<>();

                    list.add(new Item("LOl", R.drawable.file_icon));
                    list.add(new Item("sup", R.drawable.file_icon));


                    openArduinoFilePicker(list);

                } else {
                    // there is no connection
                    // display message to user that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Could not open the file explorer", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the current connection
                // not tested yet
                byte[] stateToBeSent = {1};
                MainActivity.connectThread.connectedThread.write(stateToBeSent);
                MainActivity.protocolState = 1;

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

                        new MaterialDialog.Builder(PostGetActivity.this)
                                .content(getString(R.string.bluetooth_disabled_message))
                                .cancelable(false)
                                .title(R.string.error)
                                .negativeColor(Color.GRAY)
                                .icon(getResources().getDrawable(R.drawable.ic_alert))
                                .positiveText(R.string.yes)
                                .negativeText(R.string.no)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        Intent intent = new Intent(PostGetActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra(MainActivity.TAG, MainActivity.EXTRA_ENABLE_BLUETOOTH);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                        closeApplication(getString(R.string.error_bluetooth_not_enabled));
                                    }
                                })
                                .show();
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
        new MaterialDialog.Builder(PostGetActivity.this)
                .content(message)
                .cancelable(false)
                .title(R.string.error)
                .icon(getResources().getDrawable(R.drawable.ic_alert))
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent intent = new Intent(PostGetActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                })
                .show();
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

    private void openAndroidFilePicker() {
        loadFileList();

        // Use the Builder class for convenient dialog construction
        final MaterialDialog dialog = new MaterialDialog.Builder(PostGetActivity.this)
                .title(R.string.file_explorer_dialog_title)
                .adapter(new FileArrayAdapter(PostGetActivity.this, fileList))
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        statusTextView.setText("Cancelled");
                    }

                })
                .build();


        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    chosenFile = fileList.get(position).file;
                    File sel = new File(path + "/" + chosenFile);

                    if (sel.isDirectory()) {
                        firstLvl = false;

                        // Adds chosen directory to list
                        str.add(chosenFile);
                        fileList = null;
                        path = new File(sel + "");

                        loadFileList();

                        onAndroidFileClick(sel, true);
                        dialog.dismiss();
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

                        onAndroidFileClick(sel, true);
                        dialog.dismiss();

                    }
                    // File picked
                    else {
                        onAndroidFileClick(sel, false);
                        dialog.dismiss();
                    }
                }
            });
        }

        dialog.show();
    }

    private void openArduinoFilePicker(ArrayList<Item> array) {

        progressBar.setVisibility(View.GONE);

        if (isExternalStorageWritable()) {

            final MaterialDialog dialog = new MaterialDialog.Builder(PostGetActivity.this)
                    .title("Choose a File")
                    .adapter(new FileArrayAdapter(PostGetActivity.this, array))

                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            statusTextView.setText("Cancelled");
                        }
                    })
                    .show();

            ListView listView = dialog.getListView();
            if (listView != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "You chose" + position, Toast.LENGTH_SHORT).show();
                        statusTextView.setText("Getting File");
                        progressBar.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
            }

        } else {
            // there is no connection
            // display message to user that there is no connection
            Toast.makeText(getApplicationContext(),
                    "Could not open the file explorer", Toast.LENGTH_SHORT).show();
        }

    }



    public void onAndroidFileClick(File file, Boolean again) {
        // user chose an item

        if (again) {
            // chose a directory
            openAndroidFilePicker();
        } else {
            // chose a file

            statusTextView.setText("Sending File");
            progressBar.setVisibility(View.VISIBLE);

            try {
                sendFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
       // convert items to arraylist

                /*openArduinoFilePicker(arduinoFilesArrayList);*/
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


    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    ------------------------------------------------------------------------
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


    private void loadFileList() {
        if (path.mkdirs() || path.isDirectory()) {
            // able to write to sd card
        } else {
            // unable to write on the sd card
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
            fileList = new ArrayList<>();
            //fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                //fileList[i] = new Item(fList[i], R.drawable.file_icon);
                fileList.add(new Item(fList[i], R.drawable.file_icon)) ;

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList.get(i).icon = R.drawable.directory_icon;
                } else {

                }
            }

            if (!firstLvl) {
                fileList.add(0,  new Item("Up", R.drawable.directory_up));
            }



        } else {
            // path does not exist
        }


    }
}
