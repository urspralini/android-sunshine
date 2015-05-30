package com.example.pbabu.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.pbabu.sunshine.app.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter forecastAdapter;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        ListView forecastListView = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        //add item click listener
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    Long weatherDate = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
                    String location = Utility.getPreferredLocation(getActivity());
                    Uri detailedLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, weatherDate);
                    Intent detailIntent = new Intent(getActivity(), ForecastDetailActivity.class);
                    detailIntent.setData(detailedLocationUri);
                    startActivity(detailIntent);
                }
            }
        });
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
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "ForecastFragment.onResume");
        super.onResume();
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
        if (item.getItemId() == R.id.action_refresh) {
            fetchWeatherForeCast();
            return true;
        } else if (item.getItemId() == R.id.action_view_loc_on_map) {
            showLocationOnMap();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
    }

    private void fetchWeatherForeCast() {
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        fetchWeatherTask.execute(location);
    }

    private void showLocationOnMap() {
        String location = Utility.getPreferredLocation(getActivity());
        //create an implicit intent to open the location on the map
        Uri locationUri = Uri.parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW)
                .setData(locationUri);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
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
        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        fetchWeatherForeCast();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
}
