package com.example.pbabu.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.pbabu.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = ForecastDetailActivityFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private static final String HASH_TAG = "#sunshineApp";
    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;
    private static final String[] FORECAST_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_DEGREES = 7;
    private TextView dayTextView;
    private TextView dateTextView;
    private TextView highTextView;
    private TextView lowTextView;
    private ImageView iconView;
    private TextView descTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView pressureTextView;
    private Context context;

    public ForecastDetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "ForecastDetailActivityFragment.onDestroy");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View detailForecastView = inflater.inflate(R.layout.fragment_forecast_detail, container, false);
        context = detailForecastView.getContext();
        dayTextView = (TextView) detailForecastView.findViewById(R.id.list_item_day_textview);
        dateTextView = (TextView) detailForecastView.findViewById(R.id.list_item_date_textview);
        lowTextView = (TextView) detailForecastView.findViewById(R.id.list_item_low_textview);
        highTextView = (TextView) detailForecastView.findViewById(R.id.list_item_high_textview);
        iconView = (ImageView)detailForecastView.findViewById(R.id.list_item_icon);
        descTextView = (TextView) detailForecastView.findViewById(R.id.list_item_forecast_textview);
        humidityTextView = (TextView) detailForecastView.findViewById(R.id.list_item_humidity_textview);
        windSpeedTextView = (TextView) detailForecastView.findViewById(R.id.list_item_wind_textview);
        pressureTextView = (TextView) detailForecastView.findViewById(R.id.list_item_pressure_textview);
        Intent intent = getActivity().getIntent();
        if(intent != null) {
            mForecastStr = intent.getDataString();
        }
        return detailForecastView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_detail_fragment, menu);
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        if(mShareActionProvider != null && mForecastStr != null) {
            mShareActionProvider.setShareIntent(buildShareIntent());
        }else {
            Log.d(LOG_TAG, "share action provider is null or forecast string is null?");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        final Intent intent = getActivity().getIntent();
        if(intent == null) return null;
        return new CursorLoader(getActivity(), intent.getData(), FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(!cursor.moveToFirst()) return;
        mForecastStr = convertCursorRowToUXFormat(cursor);
        //fetch the values from the cursor and populate the views
        final long dateInMilliSecs = cursor.getLong(COL_WEATHER_DATE);
        //set the day
        dayTextView.setText(Utility.getDayName(context, dateInMilliSecs));
        //set the date
        dateTextView.setText(Utility.getFormattedMonthDay(context, dateInMilliSecs));
        boolean isMetric = Utility.isMetric(context);
        //set the high temp
        final double highTemp = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        highTextView.setText(Utility.formatTemperature(context, highTemp, isMetric));
        //set the low temp
        final double lowTemp = cursor.getDouble(COL_WEATHER_MIN_TEMP);
        lowTextView.setText(Utility.formatTemperature(context, lowTemp, isMetric));
        //set the weather icon
        iconView.setImageResource(R.drawable.ic_launcher);
        //set desc
        final String description = cursor.getString(COL_WEATHER_DESC);
        descTextView.setText(description);
        //set the humidity
        final double humidity = cursor.getDouble(COL_WEATHER_HUMIDITY);
        humidityTextView.setText(context.getString(R.string.format_humidity, humidity));
        //set the windspeed
        final float windSpeed = cursor.getFloat(COL_WEATHER_WIND_SPEED);
        final float degrees = cursor.getFloat(COL_WEATHER_DEGREES);
        windSpeedTextView.setText(Utility.getFormattedWind(context, windSpeed, degrees));
        //set the pressure
        final float pressure = cursor.getFloat(COL_WEATHER_PRESSURE);
        pressureTextView.setText(context.getString(R.string.format_pressure, pressure));
        //If onCreateOptionsMenu has already happened, then we need to update the shareIntent
        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(buildShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Intent buildShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, mForecastStr + HASH_TAG);
        return shareIntent;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(getActivity());
        String highLowStr = Utility.formatTemperature(this.getActivity(), high, isMetric) + "/" +
                Utility.formatTemperature(this.getActivity(), low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        String highAndLow = formatHighLows(
                cursor.getDouble(COL_WEATHER_MAX_TEMP),
                cursor.getDouble(COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(COL_WEATHER_DATE)) +
                " - " + cursor.getString(COL_WEATHER_DESC) +
                " - " + highAndLow;
    }
}
