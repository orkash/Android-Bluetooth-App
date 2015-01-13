package com.maddies.arduinobluetoothapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // creates the preferences from the xml preferences file
        addPreferencesFromResource(R.xml.preferences);


        // not working yet
        /*Preference restoreSettingsButton = findPreference("restore_settings");
        restoreSettingsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Toast.makeText(getActivity(), "Default settings set", Toast.LENGTH_SHORT).show();
                // resets settings
                SharedPreferences prefs = getActivity().getSharedPreferences("preferences.xml",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                return true;
            }
        });*/
    }
}


