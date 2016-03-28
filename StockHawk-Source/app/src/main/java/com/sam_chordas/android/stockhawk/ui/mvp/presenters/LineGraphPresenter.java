package com.sam_chordas.android.stockhawk.ui.mvp.presenters;

import android.content.Intent;

import com.sam_chordas.android.stockhawk.StockHawkApp;
import com.sam_chordas.android.stockhawk.events.DataEvent;
import com.sam_chordas.android.stockhawk.service.RetrieveStockHistoricalDataService;
import com.sam_chordas.android.stockhawk.ui.mvp.views.LineGraphView;

/**
 * Created by aung on 3/27/16.
 */
public class LineGraphPresenter extends BasePresenter {

    private final LineGraphView mView;
    private final String mStockSymbol;

    public LineGraphPresenter(LineGraphView mView, String stockSymbol) {
        this.mView = mView;
        this.mStockSymbol = stockSymbol;
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    public void retrieveStockHistoricalData(String startDate, String endDate) {
        Intent intent = RetrieveStockHistoricalDataService.newIntent(mStockSymbol, startDate, endDate);
        StockHawkApp.getContext().startService(intent);
    }

    public void onEventMainThread(DataEvent.LoadedStockHistoricalDataEvent event) {
        mView.displayHistoricalQuoteData(event.getQuoteList());
    }

    public void onEventMainThread(DataEvent.PickedStartDateEvent event) {
        mView.displayPickedStartDate(event.getStartDate());
    }

    public void onEventMainThread(DataEvent.PickedEndDateEvent event) {
        mView.displayPickedEndDate(event.getEndDate());
    }
}
