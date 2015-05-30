package com.example.pbabu.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import com.example.pbabu.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter forecastAdapter;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        ListView forecastListView = (ListView)fragmentView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        return fragmentView;
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "ForecastFragment.onCreate");
        super.onCreate(savedInstanceState);
        //add this line to enable this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "ForecastFragment.onStart");
        super.onStart();
        //fetch weather forecast for the current user preferences in the foreground
        fetchWeatherForeCast();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "ForecastFragment.onResume");
        super.onResume();
        fetchWeatherForeCast();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "ForecastFragment.onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "ForecastFragment.onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "ForecastFragment.onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh) {
            fetchWeatherForeCast();
            return true;
        }else if (item.getItemId() == R.id.action_view_loc_on_map) {
            showLocationOnMap();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
    }

    private void fetchWeatherForeCast(){
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        fetchWeatherTask.execute(location);
    }

    private void showLocationOnMap(){
        String location = Utility.getPreferredLocation(getActivity());
        //create an implicit intent to open the location on the map
        Uri locationUri = Uri.parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW)
                .setData(locationUri);
        if(mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }else {
            Log.d(LOG_TAG, "Could not call map view intent for location:" + location);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri,null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }
}
