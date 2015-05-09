package com.example.pbabu.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
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
}
