package com.maddies.arduinobluetoothapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class PostGetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_get);

        TextView connectedTo = (TextView) findViewById(R.id.connected_to_text_view);

        Intent intent = getIntent();
        String device = intent.getExtras().getString("device name and address");

        connectedTo.setText("Connected to: " + device);

        // When the select file button is open the file explorer and send it
        Button postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (MainActivity.bluetoothThread.connectedThread.isAlive()) {
                    // there is a connection
                    // open the explorer
                    Intent intent = new Intent(PostGetActivity.this, FileExplore.class);
                    startActivity(intent);
                } else {
                    // there is no connection
                    // display message to user that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Before selecting a file you need to be connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button getButton = (Button) findViewById(R.id.get_button);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if there is  a bluetooth connection with an Arduino
                if (MainActivity.bluetoothThread.connectedThread.isAlive()) {
                    // there is a connection
                    Toast.makeText(getApplicationContext(),
                            "Before selecting a file you need to be connected", Toast.LENGTH_SHORT).show();
                } else {
                    // there is no connection
                    // display message to user that there is no connection
                    Toast.makeText(getApplicationContext(),
                            "Before selecting a file you need to be connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Toast toast = Toast.makeText(getApplicationContext(), "Developed by Matthijs & Maarten", Toast.LENGTH_SHORT);
                toast.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
