package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.sam_chordas.android.stockhawk.StockHawkApp;
import com.sam_chordas.android.stockhawk.data.vos.QuoteVO;
import com.sam_chordas.android.stockhawk.events.DataEvent;
import com.sam_chordas.android.stockhawk.utils.CommonInstances;
import com.sam_chordas.android.stockhawk.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by aung on 3/27/16.
 */
public class RetrieveStockHistoricalDataService extends IntentService {

    private static final String IE_STOCK_SYMBOL = "IE_STOCK_SYMBOL";
    private static final String IE_START_DATE = "IE_START_DATE";
    private static final String IE_END_DATE = "IE_END_DATE";

    public static Intent newIntent(String stockSymbol, String startDate, String endDate) {
        Intent intent = new Intent(StockHawkApp.getContext(), RetrieveStockHistoricalDataService.class);
        intent.putExtra(IE_STOCK_SYMBOL, stockSymbol);
        intent.putExtra(IE_START_DATE, startDate);
        intent.putExtra(IE_END_DATE, endDate);
        return intent;
    }

    public RetrieveStockHistoricalDataService() {
        super(RetrieveStockHistoricalDataService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockHawkApp.LOG_TAG, "RetrieveStockHistoricalDataService - onHandleIntent");
        String stockSymbol = intent.getStringExtra(IE_STOCK_SYMBOL);
        String startDate = intent.getStringExtra(IE_START_DATE);
        String endDate = intent.getStringExtra(IE_END_DATE);

        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("SELECT * FROM yahoo.finance.historicaldata WHERE symbol = "
                    + "\"" + stockSymbol + "\" AND startDate = \"" + startDate + "\" AND endDate = \"" + endDate + "\"", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String response;

        urlString = urlStringBuilder.toString();
        try {
            response = NetworkUtils.getInstance().fetchData(urlString);
            try {
                JSONObject rootJson = new JSONObject(response);
                JSONObject queryJson = rootJson.getJSONObject("query");
                JSONArray quoteJsonArray = queryJson.getJSONObject("results").getJSONArray("quote");

                Type listType = new TypeToken<List<QuoteVO>>() {
                }.getType();
                List<QuoteVO> quoteList = CommonInstances.getGsonInstance().fromJson(quoteJsonArray.toString(), listType);

                DataEvent.LoadedStockHistoricalDataEvent event = new DataEvent.LoadedStockHistoricalDataEvent(quoteList);
                EventBus.getDefault().post(event);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
