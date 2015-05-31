package com.example.pbabu.sunshine.app;

/**
 * Created by pbabu on 5/23/15.
 */
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pbabu.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //bind the view with values from the database
        ImageView iconView = (ImageView)view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);

        //get date from cursor
        final long dateInMillisecs = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        //set date text view
        TextView dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(this.mContext,dateInMillisecs));

        boolean isMetric = Utility.isMetric(this.mContext);
        //get max temp from cursor
        final Double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //set high temp text view
        TextView highTextView = (TextView)view.findViewById(R.id.list_item_high_textview);
        highTextView.setText(Utility.formatTemperature(maxTemp, isMetric));

        //get min temp from cursor
        final double lowTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //set low temp text view
        TextView lowTextView = (TextView)view.findViewById(R.id.list_item_low_textview);
        lowTextView.setText(Utility.formatTemperature(lowTemp, isMetric));

        //get weather description from cursor
        final String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //set description text view
        TextView descTextView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        descTextView.setText(desc);

    }
}
