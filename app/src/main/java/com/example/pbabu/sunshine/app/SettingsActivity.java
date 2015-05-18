package com.example.pbabu.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by pbabu on 5/16/15.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //call on sharedPreferenceChanged to set summary units
        setSummaryForPreferences();
    }

    private void setSummaryForPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPref, getString(R.string.pref_units_key));
        onSharedPreferenceChanged(sharedPref, getString(R.string.pref_location_key));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_units_key))){
            ListPreference unitsPreference = (ListPreference)findPreference(key);
            String value = sharedPreferences.getString(key, "");
            int indexOfValue = unitsPreference.findIndexOfValue(value);
            if(indexOfValue>=0){
                unitsPreference.setSummary(unitsPreference.getEntries()[indexOfValue]);
            }
        }else {
            Preference locationPreference = findPreference(key);
            locationPreference.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
