package com.sam_chordas.android.stockhawk.ui.views.items;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by aung on 3/27/16.
 */
public class ViewItemQuote extends LinearLayout {

    @Bind(R.id.stock_symbol)
    TextView tvStockSymbol;

    @Bind(R.id.stock_name)
    TextView tvName;

    @Bind(R.id.bid_price)
    TextView tvBidPrice;

    @Bind(R.id.change)
    TextView tvChange;

    private Drawable redBgDrawable, greenBgDrawable;

    public ViewItemQuote(Context context) {
        super(context);
    }

    public ViewItemQuote(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewItemQuote(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            redBgDrawable = getContext().getResources().getDrawable(R.drawable.percent_change_pill_red, getContext().getTheme());
            greenBgDrawable = getContext().getResources().getDrawable(R.drawable.percent_change_pill_green, getContext().getTheme());
        } else {
            redBgDrawable = getContext().getResources().getDrawable(R.drawable.percent_change_pill_red);
            greenBgDrawable = getContext().getResources().getDrawable(R.drawable.percent_change_pill_green);
        }
    }

    public void setData(Cursor cursor) {
        tvName.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME)));
        tvStockSymbol.setText(getContext().getString(R.string.format_stock_symbol, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL))));
        tvBidPrice.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));

        if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                tvChange.setBackgroundDrawable(greenBgDrawable);
            } else {
                tvChange.setBackground(greenBgDrawable);
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                tvChange.setBackgroundDrawable(redBgDrawable);
            } else {
                tvChange.setBackground(redBgDrawable);
            }
        }

        if (Utils.showPercent) {
            tvChange.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        } else {
            tvChange.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
        }
    }
}
