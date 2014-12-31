package com.maddies.arduinobluetoothapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // creates the preferences from the xml preferences file
        addPreferencesFromResource(R.xml.preferences);
    }
}


