package com.sam_chordas.android.stockhawk.data.vos;

import com.google.gson.annotations.SerializedName;
import com.sam_chordas.android.stockhawk.utils.StockHawkConstants;

import java.util.List;

/**
 * Created by aung on 3/27/16.
 */
public class QuoteVO {

    @SerializedName("Symbol")
    private String symbol;

    @SerializedName("Date")
    private String dateText;

    @SerializedName("Open")
    private String open;

    @SerializedName("High")
    private String high;

    @SerializedName("Low")
    private String low;

    @SerializedName("Close")
    private String close;

    @SerializedName("Volume")
    private String volume;

    @SerializedName("Adj_Close")
    private String adjClose;

    private static final int CHART_MAX_LIMIT = 20;

    public String getSymbol() {
        return symbol;
    }

    public String getDateText() {
        return dateText;
    }

    public String getOpen() {
        return open;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getClose() {
        return close;
    }

    public String getVolume() {
        return volume;
    }

    public String getAdjClose() {
        return adjClose;
    }

    public static String[] getLabels(List<QuoteVO> quoteList) {
        String[] labels = new String[quoteList.size()];

        for (int index = 0; index < quoteList.size(); index++) {
            labels[index] = ""; //DateUtils.sdfChartByDate.format(DateUtils.sdfQueryDate.parse(quoteList.get(index).getDateText()));
        }

        return labels;
    }

    public static float[][] getChartData(List<QuoteVO> quoteList, int dataToProject) {
        float[][] chartData = new float[1][quoteList.size()];
        //float maxClose = getMaxClose(quoteList);
        for (int index = 0; index < quoteList.size(); index++) {
            float close;

            switch (dataToProject) {
                case StockHawkConstants.OPEN_BID:
                   close = Float.parseFloat(quoteList.get(index).getOpen());
                    break;
                case StockHawkConstants.HIGHEST_BID:
                    close = Float.parseFloat(quoteList.get(index).getHigh());
                    break;
                case StockHawkConstants.LOWEST_BID:
                    close = Float.parseFloat(quoteList.get(index).getLow());
                    break;
                case StockHawkConstants.CLOSE_BID:
                    close = Float.parseFloat(quoteList.get(index).getClose());
                    break;
                case StockHawkConstants.ADJ_CLOSE_BID:
                    close = Float.parseFloat(quoteList.get(index).getAdjClose());
                    break;
                default:
                    close = 0;
            }

            chartData[0][index] = close;// * CHART_MAX_LIMIT / maxClose;
        }

        return chartData;
    }

    public static float getMaxClose(List<QuoteVO> quoteList) {
        float maxClose = 0;
        for (QuoteVO quote : quoteList) {
            float close = Float.parseFloat(quote.getClose());
            if (maxClose < close) {
                maxClose = close;
            }
        }

        return maxClose;
    }
}
