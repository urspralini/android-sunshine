package com.example.pbabu.sunshine.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.pbabu.sunshine.app.data.WeatherContract;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mLocation;
    private boolean mTwoPane;
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "MainActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mLocation = Utility.getPreferredLocation(this);
        if(findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.weather_detail_container,
                                new ForecastDetailActivityFragment(),
                                DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "MainActivity.onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "MainActivity.onResume");
        super.onResume();
        final String locationSetting = Utility.getPreferredLocation(this);
        if(!mLocation.equals(locationSetting)){
            //do something
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(ff != null) {
                ff.onLocationChanged();
            }
            ForecastDetailActivityFragment df = (ForecastDetailActivityFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if(df != null) {
                df.onLocationChanged(locationSetting);
            }
            mLocation = locationSetting;
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "MainActivity.onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "MainActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "MainActivity.onDestroy");
        super.onDestroy();
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
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivityIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri detailWeatherUri) {
        if(mTwoPane){
            ForecastDetailActivityFragment detailFragment = ForecastDetailActivityFragment
                    .newInstance(detailWeatherUri);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }else {
            Intent detailIntent = new Intent(this, ForecastDetailActivity.class);
            detailIntent.setData(detailWeatherUri);
            startActivity(detailIntent);
        }
    }

    public boolean isTwoPane(){
        return mTwoPane;
    }
}
