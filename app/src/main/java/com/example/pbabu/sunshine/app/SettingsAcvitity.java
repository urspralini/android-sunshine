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
public class SettingsAcvitity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_UNITS = "pref_units";
    public static final String PREF_LOCATION = "pref_location";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //call on sharedPreferenceChanged to set summary units
        setSummaryForPreferences();
    }

    private void setSummaryForPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        onSharedPreferenceChanged(sharedPref, PREF_UNITS);
        onSharedPreferenceChanged(sharedPref, PREF_LOCATION);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PREF_UNITS)){
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
