package com.maddies.arduinobluetoothapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Display the fragment as the main content.

        getFragmentManager().beginTransaction().replace(R.id.preference_container, new SettingsFragment())
                .commit();
    }
}