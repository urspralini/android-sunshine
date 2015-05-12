package com.example.pbabu.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailActivityFragment extends Fragment {

    public ForecastDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View forecastDetailFragment = inflater.inflate(R.layout.fragment_forecast_detail, container, false);
        TextView textView = (TextView) forecastDetailFragment.findViewById(R.id.detail_text);
        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String forecastText = intent.getStringExtra(Intent.EXTRA_TEXT);
            textView.setText(forecastText);
        }
        return forecastDetailFragment;
    }
}
