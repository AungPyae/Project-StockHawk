package com.sam_chordas.android.stockhawk.events;

import com.sam_chordas.android.stockhawk.data.vos.QuoteVO;

import java.util.Date;
import java.util.List;

/**
 * Created by aung on 3/27/16.
 */
public class DataEvent {

    public static class LoadedStockHistoricalDataEvent {
        private final List<QuoteVO> quoteList;

        public LoadedStockHistoricalDataEvent(List<QuoteVO> quoteList) {
            this.quoteList = quoteList;
        }

        public List<QuoteVO> getQuoteList() {
            return quoteList;
        }
    }

    public static class PickedStartDateEvent {
        private final Date startDate;

        public PickedStartDateEvent(Date startDate) {
            this.startDate = startDate;
        }

        public Date getStartDate() {
            return startDate;
        }
    }

    public static class PickedEndDateEvent {
        private final Date endDate;

        public PickedEndDateEvent(Date endDate) {
            this.endDate = endDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }
}
