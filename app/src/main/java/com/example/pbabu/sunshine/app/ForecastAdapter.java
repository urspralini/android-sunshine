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
    private static final int FORECAST_VIEW_TYPE_TODAY = 0;
    private static final int FORECAST_VIEW_TYPE_FUTURE = 1;
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
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType == FORECAST_VIEW_TYPE_TODAY) ?
                R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        //bind the view with values from the database
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        //get date from cursor
        final long dateInMillisecs = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        //set date text view
        viewHolder.dateView.setText(Utility.getFriendlyDayString(this.mContext, dateInMillisecs));

        boolean isMetric = Utility.isMetric(this.mContext);
        //get max temp from cursor
        final Double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //set high temp text view
        viewHolder.highTextView.setText(Utility.formatTemperature(maxTemp, isMetric));

        //get min temp from cursor
        final double lowTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //set low temp text view
        viewHolder.lowTextView.setText(Utility.formatTemperature(lowTemp, isMetric));

        //get weather description from cursor
        final String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //set description text view
        viewHolder.descTextView.setText(desc);

    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0)? FORECAST_VIEW_TYPE_TODAY: FORECAST_VIEW_TYPE_FUTURE;
    }


    private static class ViewHolder {
        private ImageView iconView;
        private TextView descTextView;
        private TextView highTextView;
        private TextView lowTextView;
        private TextView dateView;
        public ViewHolder(View view) {
            iconView = (ImageView)view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            highTextView = (TextView)view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView)view.findViewById(R.id.list_item_low_textview);
            descTextView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        }
    }
}
