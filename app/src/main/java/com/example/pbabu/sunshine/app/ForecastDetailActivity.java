package com.example.pbabu.sunshine.app;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.pbabu.sunshine.app.sync.SunshineSyncAdapter;


public class ForecastDetailActivity extends ActionBarActivity {

    private static final String LOG_TAG = ForecastDetailActivity.class.getSimpleName();
    private boolean mIsMetric;
    public static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //cancel if there is any notification with id:3004
        NotificationManager notificationManager =
                (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SunshineSyncAdapter.WEATHER_NOTIFICATION_ID);
        Log.d(LOG_TAG, "ForecastDetailActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);
        mIsMetric = Utility.isMetric(this);
        if(savedInstanceState == null) {
            ForecastDetailActivityFragment df = ForecastDetailActivityFragment.newInstance(getIntent().getData());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.weather_detail_container, df, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "ForecastDetailActivity.onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "ForecastDetailActivity.onResume");
        super.onResume();
        final boolean currentMetric = Utility.isMetric(this);
        ForecastDetailActivityFragment df = (ForecastDetailActivityFragment)getSupportFragmentManager()
                .findFragmentByTag(DETAIL_FRAGMENT_TAG);
        if(mIsMetric != currentMetric) {
            df.onUnitsChanged();
            mIsMetric = currentMetric;
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "ForecastDetailActivity.onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "ForecastDetailActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "ForecastDetailActivity.onDestroy");
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forecast_detail, menu);
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
}
