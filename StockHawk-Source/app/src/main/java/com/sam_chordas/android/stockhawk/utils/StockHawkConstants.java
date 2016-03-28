package com.sam_chordas.android.stockhawk.utils;

import android.content.res.Resources;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawkApp;

/**
 * Created by aung on 3/27/16.
 */
public class StockHawkConstants {

    public static final String[] STOCK_PRICE_TYPE_ARRAY;

    static {
        Resources resources = StockHawkApp.getContext().getResources();
        STOCK_PRICE_TYPE_ARRAY = new String[]{
                resources.getString(R.string.price_type_open_bid),
                resources.getString(R.string.price_type_highest_bid),
                resources.getString(R.string.price_type_lowest_bid),
                resources.getString(R.string.price_type_close_bid),
                resources.getString(R.string.price_type_adj_close_bid)
        };
    }

    public static final int OPEN_BID = 0;
    public static final int HIGHEST_BID = 1;
    public static final int LOWEST_BID = 2;
    public static final int CLOSE_BID = 3;
    public static final int ADJ_CLOSE_BID = 4;
}
