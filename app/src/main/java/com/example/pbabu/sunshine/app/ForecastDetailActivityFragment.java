package com.example.pbabu.sunshine.app;

import android.content.Intent;
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
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailActivityFragment extends Fragment {
    private static final String LOG_TAG = ForecastDetailActivityFragment.class.getSimpleName();
    private static final String HASH_TAG = "#sunshineApp";
    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;
    public ForecastDetailActivityFragment() {
        setHasOptionsMenu(true);
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
            mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            textView.setText(mForecastStr);
        }
        return forecastDetailFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_detail_fragment, menu);
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(buildShareIntent());
        }else {
            Log.d(LOG_TAG, "share action provider is null?");
        }
    }

    private Intent buildShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, mForecastStr+HASH_TAG);
        return shareIntent;
    }
}
