package com.example.pbabu.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] sampleWeatherForecastArr = new String[]{
                "Today - Sunny 88/63",
                "Tomorrow - Rainy - 50/40",
                "Weds - Foggy - 60/50",
                "Thu - Sunny - 88/63",
                "Fri - Foggy - 50/40",
                "Sat - Sunny - 60/50"
        };
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> sampleWeatherForecast = Arrays.asList(sampleWeatherForecastArr);
        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(
                //current context,fragment's parent activity
                getActivity(),
                //id of list item layout
                R.layout.list_item_forecast,
                //id of text view to populate
                R.id.list_item_forecast_textview,
                //data list
                sampleWeatherForecast
        );
        ListView forecastListView = (ListView)fragmentView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add this line to enable this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh) {
            final String MV_POSTAL_CODE = "94043";
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute(MV_POSTAL_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, Void> {
        private  final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            final String DEFAULT_POSTAL_CODE = "95134"; // default postal code
            String postalCode = null;
            if(params == null || params.length == 0){
                postalCode = DEFAULT_POSTAL_CODE;
            }else {
                postalCode = params[0];
            }
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
                final String QUERY_PARAM = "q";
                final String MODE_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String CNT_PARAM = "cnt";
                final String modeJson = "json";
                String unitMetric = "metric";
                int numOfDays = 7;
                Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, postalCode)
                        .appendQueryParameter(MODE_PARAM, modeJson)
                        .appendQueryParameter(UNITS_PARAM, unitMetric)
                        .appendQueryParameter(CNT_PARAM, Integer.toString(numOfDays));
                Uri uri = uriBuilder.build();
                String uriStr = uri.toString();
                Log.v(LOG_TAG, "url:" + uriStr);
                URL url = new URL(uriStr);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
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
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
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
            return null;
        }
    }
}
