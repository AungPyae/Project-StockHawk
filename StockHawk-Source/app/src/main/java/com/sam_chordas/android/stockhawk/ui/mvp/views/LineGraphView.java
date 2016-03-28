package com.sam_chordas.android.stockhawk.ui.mvp.views;

import com.sam_chordas.android.stockhawk.data.vos.QuoteVO;

import java.util.Date;
import java.util.List;

/**
 * Created by aung on 3/27/16.
 */
public interface LineGraphView {

    void displayHistoricalQuoteData(List<QuoteVO> quoteList);

    void displayPickedStartDate(Date startDate);

    void displayPickedEndDate(Date endDate);
}
