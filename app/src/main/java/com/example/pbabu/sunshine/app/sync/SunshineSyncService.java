package com.example.pbabu.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by pbabu on 6/5/15.
 */
public class SunshineSyncService extends Service {

    private SunshineSyncAdapter syncAdapter = null;
    private static final Object SINGLETON_LOCK = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (SINGLETON_LOCK){
            if(syncAdapter == null) {
                syncAdapter = new SunshineSyncAdapter(this, true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
