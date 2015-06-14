package com.example.pbabu.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.example.pbabu.sunshine.app.ForecastDetailActivity;
import com.example.pbabu.sunshine.app.MainActivity;
import com.example.pbabu.sunshine.app.R;
import com.example.pbabu.sunshine.app.Utility;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by pbabu on 6/5/15.
 */
public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    //Sync every 3 hrs
    private static final int SYNC_INTERVAL = 60 * 180;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();
    private Context mContext;
    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public static final int WEATHER_NOTIFICATION_ID = 3004;

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "On PerformSync is called");
        fetchWeatherDataForLocationSetting();
    }

    /**
     * Util method to start the sync adapter manually.
     * @param context
     */
    public static void syncImmediately(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority),
                bundle);
    }

    /***
     * Create a dummy account to be used by the account manager,
     * if one is not created already.
     * @param context
     * @return
     */
    public static Account getSyncAccount(Context context) {
        AccountManager accMgr = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account("Sunshine", context.getString(R.string.sync_account_type));
        /**
         * if password does not exist, then account does not exist
         */
        if(null == accMgr.getPassword(newAccount)){
            //Create a dummy account with no password and null user data
            if(!accMgr.addAccountExplicitly(newAccount, "", null)){
                return null;
            }
            onAccountCreated(newAccount,context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context){
        //configure periodic sync
        configurePeriodicSync(context, newAccount);

        //enable sync automatically, otherwise configurePeriodicSync does not work
        context.getContentResolver().
                setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        //sync immediately
        syncImmediately(context);
    }

    private static void configurePeriodicSync(Context context, Account newAccount){
        final String authority = context.getString(R.string.content_authority);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SyncRequest syncRequest = new SyncRequest.Builder()
                    .setSyncAdapter(newAccount, authority)
                    .syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME)
                    .setExtras(new Bundle()).build();
            context.getContentResolver().requestSync(syncRequest);
        }else {
            context.getContentResolver().addPeriodicSync(newAccount,
                    authority, new Bundle(), SYNC_INTERVAL);
        }
    }

    public static void initialize(Context context) {
        getSyncAccount(context);
    }

    private void fetchWeatherDataForLocationSetting() {
        final String locationQuery = Utility.getPreferredLocation(mContext);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 7;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.e(LOG_TAG, "InputStream is null?");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                Log.e(LOG_TAG, "InputStream is empty?");
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getWeatherDataFromJson(forecastJsonStr, locationQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        ContentResolver resolver = mContext.getContentResolver();
        String[] projection = new String[]{WeatherContract.LocationEntry._ID, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING};
        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?";
        String[] selectionArgs = new String[]{locationSetting};
        Cursor cursor = resolver.query(WeatherContract.LocationEntry.CONTENT_URI, projection,
                selection, selectionArgs, null);
        long locationId = -1;
        if(cursor.moveToFirst()){
            // If it exists, return the current ID
            final int IdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = cursor.getLong(IdIndex);
        }else {
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues newLocation = new ContentValues();
            newLocation.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            newLocation.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            newLocation.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            newLocation.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
            final Uri resultUri = resolver.insert(WeatherContract.LocationEntry.CONTENT_URI, newLocation);
            locationId = Long.valueOf(resultUri.getPathSegments().get(1));
        }
        return locationId;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                //delete all old data for current location setting
                deleteOldDataForLocationSetting();
                // Student: call bulkInsert to add the weatherEntries to the database here
                mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,
                        cVVector.toArray(new ContentValues[cVVector.size()]));
                notifyWeather();
                Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyWeather() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);
        final long currentTimeMillis = System.currentTimeMillis();
        boolean isNotificaionEnabled = prefs.getBoolean(context.getString(R.string.pref_notification_key),true);
        if(currentTimeMillis - lastSync >= DAY_IN_MILLIS && isNotificaionEnabled) {
            final ContentResolver contentResolver = context.getContentResolver();
            String locationSetting = Utility.getPreferredLocation(context);
            final Uri todayWeatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, currentTimeMillis);
            final Cursor cursor = contentResolver.query(todayWeatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
            if(cursor.moveToFirst()){
                boolean isMetric = Utility.isMetric(context);
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                String short_desc = cursor.getString(INDEX_SHORT_DESC);
                final double highTempature = cursor.getDouble(INDEX_MAX_TEMP);
                String highTemp = Utility.formatTemperature(context, highTempature,isMetric);
                final double lowTempature = cursor.getDouble(INDEX_MIN_TEMP);
                String lowTemp = Utility.formatTemperature(context, lowTempature, isMetric);
                String notificationContent = context.getString(R.string.format_notification, short_desc, highTemp, lowTemp);
                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String notificationTitle = context.getString(R.string.app_name);

                //Create and issue the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent);

                Intent notificationIntent = new Intent(context,MainActivity.class);
                notificationIntent.setData(todayWeatherUri);
                TaskStackBuilder stackBuilder= TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());

                //save that notification is already issued for today
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, currentTimeMillis);
                editor.commit();

            }
        }
    }

    /**
     * Delete all weather data that is one day old
     */
    private void deleteOldDataForLocationSetting(){
        Context context = getContext();
        final ContentResolver contentResolver = context.getContentResolver();
        String locationSetting = Utility.getPreferredLocation(context);
        Uri deleteWeatherUri = WeatherContract.WeatherEntry.CONTENT_URI;
        final Time dayTime = new Time();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        String yesterday = Long.toString(dayTime.setJulianDay(julianStartDay-1));
        String whereClause = WeatherContract.WeatherEntry.COLUMN_DATE +" <= ?";
        String[] selectionArgs = {yesterday};
        contentResolver.delete(deleteWeatherUri, whereClause, selectionArgs);
    }
}
