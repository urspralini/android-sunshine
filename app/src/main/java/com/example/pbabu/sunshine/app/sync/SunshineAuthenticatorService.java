package com.example.pbabu.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by pbabu on 6/5/15.
 */
public class SunshineAuthenticatorService extends Service{
    private SunshineAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new SunshineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
