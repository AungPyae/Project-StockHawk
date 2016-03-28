package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.content.Context;

/**
 * Created by aung on 3/27/16.
 */
public class StockHawkApp extends Application {

    public static final String LOG_TAG = "StockHawkApp";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        context = null;
    }

    public static Context getContext() {
        return context;
    }
}
