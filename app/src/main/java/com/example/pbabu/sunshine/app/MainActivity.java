package com.example.pbabu.sunshine.app;

import android.app.NotificationManager;
import android.content.Context;
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
import com.example.pbabu.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mLocation;
    private boolean mIsMetric;
    private boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "MainActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mLocation = Utility.getPreferredLocation(this);
        mIsMetric = Utility.isMetric(this);
        //clean up any notification with id:3004
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SunshineSyncAdapter.WEATHER_NOTIFICATION_ID);
        if(findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.weather_detail_container,
                                new ForecastDetailActivityFragment(),
                                ForecastDetailActivity.DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        //initialize syncadapter
        SunshineSyncAdapter.initialize(this);
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
        ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        ForecastDetailActivityFragment df = (ForecastDetailActivityFragment)getSupportFragmentManager()
                .findFragmentByTag(ForecastDetailActivity.DETAIL_FRAGMENT_TAG);
        if(!mLocation.equals(locationSetting)){
            if(ff != null) {
                ff.onLocationChanged();
            }
            if(df != null) {
                df.onLocationChanged(locationSetting);
            }
            mLocation = locationSetting;
        }else {
            final boolean currentMetric = Utility.isMetric(this);
            if(mIsMetric != currentMetric) {
                if(ff != null) {
                    ff.onUnitsChanged();
                }
                if(df != null) {
                    df.onUnitsChanged();
                }
                mIsMetric = currentMetric;
            }
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
                    .replace(R.id.weather_detail_container, detailFragment, ForecastDetailActivity.DETAIL_FRAGMENT_TAG)
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
