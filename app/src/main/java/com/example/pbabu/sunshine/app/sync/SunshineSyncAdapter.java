package com.example.pbabu.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.pbabu.sunshine.app.R;

/**
 * Created by pbabu on 6/5/15.
 */
public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();
    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Performing sync");
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
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        }
        return newAccount;
    }
}
