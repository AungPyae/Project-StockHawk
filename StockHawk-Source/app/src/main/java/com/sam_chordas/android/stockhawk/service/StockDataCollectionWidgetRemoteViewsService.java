package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawkApp;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;

/**
 * Created by aung on 3/4/16.
 */
public class StockDataCollectionWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - onCreate");
            }

            @Override
            public void onDataSetChanged() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - onDataSetChanged");
                if (data != null)
                    data.close();

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        null, //projections
                        QuoteColumns.ISCURRENT + " = ?", //selection
                        new String[]{"1"}, //selectionArgs
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - onDestroy");
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - getCount");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - getViewAt " + position);
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews rvs = new RemoteViews(getPackageName(), R.layout.widget_stock_data_item);

                rvs.setTextViewText(R.id.stock_name, data.getString(data.getColumnIndex(QuoteColumns.NAME)));
                String stockSymbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                rvs.setTextViewText(R.id.stock_symbol, getApplicationContext().getString(R.string.format_stock_symbol, stockSymbol));
                rvs.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));

                if (data.getInt(data.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                    rvs.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    rvs.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                if (Utils.showPercent) {
                    rvs.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                } else {
                    rvs.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
                }

                int rowId = data.getInt(data.getColumnIndex(QuoteColumns._ID));
                final Intent fillInIntent = LineGraphActivity.newIntent(stockSymbol, rowId);
                rvs.setOnClickFillInIntent(R.id.widget_stock_item, fillInIntent);

                return rvs;
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - getLoadingView");
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - getViewTypeCount");
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - getItemId");
                if (data != null && data.moveToPosition(position)) {
                    return data.getInt(data.getColumnIndex(QuoteColumns._ID));
                }
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                Log.d(StockHawkApp.LOG_TAG, "StockDataCollectionWidgetRemoteViewsService - hasStableIds");
                return true;
            }
        };
    }
}
