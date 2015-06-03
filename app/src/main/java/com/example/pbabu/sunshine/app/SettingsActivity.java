package com.example.pbabu.sunshine.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by pbabu on 5/16/15.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "SettingsActivity.onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //call on sharedPreferenceChanged to set summary units
        setSummaryForPreferences();
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "SettingsActivity.onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "SettingsActivity.onResume");
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "SettingsActivity.onPause");
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "SettingsActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "SettingsActivity.onDestroy");
        super.onDestroy();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
