package com.example.pbabu.sunshine.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.TextView;

import com.example.pbabu.sunshine.app.data.WeatherContract;
import com.example.pbabu.sunshine.app.sync.SunshineSyncAdapter;
import static com.example.pbabu.sunshine.app.sync.SunshineSyncAdapter.LOCATION_STATUS_OK;
import com.example.pbabu.sunshine.app.sync.SunshineSyncAdapter.LocationStatus;
import org.w3c.dom.Text;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter forecastAdapter;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private Callback mCallback = null;
    private static final String LIST_VIEW_POSITION = "LIST_VIEW_CURRENT_POSITION";
    private int listViewSelectedPosition = 0;
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
    static final int COL_HUMIDITY = 9;
    static final int COL_PRESSURE = 10;
    static final int COL_WIND_SPEED = 11;
    private ListView forecastListView;
    private TextView emptyTextView;
    private @LocationStatus int mLastSyncStatus;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastAdapter = new ForecastAdapter((MainActivity)getActivity(), null, 0);
        forecastListView = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        //add item click listener
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                listViewSelectedPosition = position;
                if (cursor != null) {
                    Long weatherDate = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
                    String location = Utility.getPreferredLocation(getActivity());
                    Uri detailedLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, weatherDate);
                    mCallback.onItemSelected(detailedLocationUri);
                }
            }
        });
        if(savedInstanceState != null && savedInstanceState.containsKey(LIST_VIEW_POSITION)) {
            listViewSelectedPosition = savedInstanceState.getInt(LIST_VIEW_POSITION);
        }
        emptyTextView = (TextView)fragmentView.findViewById(R.id.empty_view_forecast);
        forecastListView.setEmptyView(emptyTextView);
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "ForecastFragment.onPause");
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
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
        if (item.getItemId() == R.id.action_view_loc_on_map) {
            showLocationOnMap();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LIST_VIEW_POSITION, listViewSelectedPosition);
    }

    private void fetchWeatherForeCast() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private void showLocationOnMap() {
        String location = Utility.getPreferredLocation(getActivity());
        // these indices must match the projection
        final int INDEX_COLUMN_LAT = 0;
        final int INDEX_COLUMN_LONG = 1;
        Context context = getActivity();
        final ContentResolver contentResolver = context.getContentResolver();
        String locationSetting = Utility.getPreferredLocation(context);
        String[] projection = new String[]{WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG};
        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?";
        String[] selectionArgs = new String[]{locationSetting};
        Cursor cursor = contentResolver.query(WeatherContract.LocationEntry.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if(cursor.moveToFirst()){
            final String latitude = Double.toString(cursor.getDouble(INDEX_COLUMN_LAT));
            final String longitude = Double.toString(cursor.getDouble(INDEX_COLUMN_LONG));
            String geoQuery = String.format("geo:%s,%s", latitude, longitude);
            //create an implicit intent to open the location on the map
            Uri locationUri = Uri.parse("geo:0,0?")
                    .buildUpon()
                    .appendQueryParameter("q", String.format("%s,%s(Your Current Location)", latitude, longitude))
                    .build();
            Intent mapIntent = new Intent(Intent.ACTION_VIEW)
                    .setData(locationUri);
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Log.d(LOG_TAG, "Could not call map view intent for location:" + location);
            }
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
        if(listViewSelectedPosition != ListView.INVALID_POSITION) {
            forecastListView.smoothScrollToPosition(listViewSelectedPosition);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        fetchWeatherForeCast();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    public void onUnitsChanged() {
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallback = (Callback)activity;
        }catch (ClassCastException exception){
            throw new ClassCastException(activity.toString() + " must implement " + Callback.class.getSimpleName());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mLastSyncStatus = Utility.getLastSyncLocationStatus(getActivity());
        updateEmptyView();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private void updateEmptyView() {
        if(forecastAdapter.getCount() == 0) {
            String reasonMessage="";
            switch (mLastSyncStatus){
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                    final String INVALID_STATUS = "\n Backend Server is incompatible with current app version, please update Sunshine App";
                    reasonMessage = INVALID_STATUS;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                    final String SERVER_DOWN_STATUS = "\n Backend Server is down. Please try again later";
                    reasonMessage = SERVER_DOWN_STATUS;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                    final String INVALID_LOCATION = "\n Invalid Location";
                    reasonMessage = INVALID_LOCATION;
                    break;
                default:
                    if(!Utility.isInternetEnabled(getActivity())){
                        //no internet connection. update the empty view text
                        final String NO_INTERNET_CONNECTION = "\n The network is not available to fetch" +
                                " the weather data";
                        reasonMessage = NO_INTERNET_CONNECTION;
                    }
            }
            final String emptyViewText = getString(R.string.empty_view, reasonMessage);
            emptyTextView.setText(emptyViewText);
        }
    }
}
