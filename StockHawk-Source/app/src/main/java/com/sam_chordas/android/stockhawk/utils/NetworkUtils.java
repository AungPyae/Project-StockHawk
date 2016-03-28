package com.sam_chordas.android.stockhawk.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sam_chordas.android.stockhawk.StockHawkApp;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by aung on 3/27/16.
 */
public class NetworkUtils {

    private static NetworkUtils objInstance;

    private final ConnectivityManager connectivityManager;
    private final OkHttpClient client;

    public static NetworkUtils getInstance(){
        if(objInstance == null) {
            objInstance = new NetworkUtils();
        }

        return objInstance;
    }

    private NetworkUtils() {
        Context context = StockHawkApp.getContext();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        client = new OkHttpClient();
    }

    public boolean isOnline(){
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
