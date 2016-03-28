package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawkApp;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.vos.QuoteVO;
import com.sam_chordas.android.stockhawk.dialogs.DatePickerFragment;
import com.sam_chordas.android.stockhawk.ui.mvp.presenters.LineGraphPresenter;
import com.sam_chordas.android.stockhawk.ui.mvp.views.LineGraphView;
import com.sam_chordas.android.stockhawk.ui.views.ViewComponentLoader;
import com.sam_chordas.android.stockhawk.ui.views.items.ViewItemQuote;
import com.sam_chordas.android.stockhawk.utils.DateUtils;
import com.sam_chordas.android.stockhawk.utils.StockHawkConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by aung on 3/27/16.
 */
public class LineGraphActivity extends AppCompatActivity
        implements LineGraphView,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String IE_STOCK_SYMBOL = "IE_STOCK_SYMBOL";
    private static final String IE_ROW_ID = "IE_ROW_ID";

    private static final int CURSOR_LOADER_ID = 1;

    private int mRowId;
    private LineGraphPresenter mPresenter;

    @Bind(R.id.lcv_stock_overtime)
    LineChartView lcvStockOvertime;

    @Bind(R.id.lbl_chart_selection_range)
    TextView tvChartSelectionRange;

    @Bind(R.id.vc_loader)
    ViewComponentLoader vcLoader;

    @Bind(R.id.et_start_date)
    EditText etStartDate;

    @Bind(R.id.et_end_date)
    EditText etEndDate;

    @Bind(R.id.vi_today_stock)
    ViewItemQuote viTodayStock;

    @Bind(R.id.sp_data_to_project)
    Spinner spDateToProject;

    private List<QuoteVO> mQuoteList;

    public static Intent newIntent(String stockSymbol, int rowId) {
        Intent intentToLineGraph = new Intent(StockHawkApp.getContext(), LineGraphActivity.class);
        intentToLineGraph.putExtra(IE_STOCK_SYMBOL, stockSymbol);
        intentToLineGraph.putExtra(IE_ROW_ID, rowId);
        return intentToLineGraph;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        ButterKnife.bind(this, this);

        if (savedInstanceState == null) {
            String stockSymbol = getIntent().getStringExtra(IE_STOCK_SYMBOL);
            mRowId = getIntent().getIntExtra(IE_ROW_ID, -1);

            String firstDayForTheMonth = DateUtils.sdfQueryDate.format(DateUtils.getFirstDateOfCurrentMonth()); //yyyy-MM-dd
            etStartDate.setText(firstDayForTheMonth);

            String today = DateUtils.sdfQueryDate.format(new Date()); //yyyy-MM-dd
            etEndDate.setText(today);

            mPresenter = new LineGraphPresenter(this, stockSymbol);
            mPresenter.onCreate();

            List<String> priceTypeList = new ArrayList<>(Arrays.asList(StockHawkConstants.STOCK_PRICE_TYPE_ARRAY));
            ArrayAdapter<String> priceTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceTypeList);
            priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDateToProject.setAdapter(priceTypeAdapter);

            spDateToProject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    refreshGraphDisplay();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
        mPresenter.retrieveStockHistoricalData(etStartDate.getText().toString(), etEndDate.getText().toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public void displayHistoricalQuoteData(List<QuoteVO> quoteList) {
        vcLoader.dismissLoader();
        Log.d(StockHawkApp.LOG_TAG, "Stock Size : " + quoteList.size());
        mQuoteList = quoteList;

        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        String selectionRange = getString(R.string.format_chart_selection, startDate, endDate);
        tvChartSelectionRange.setText(selectionRange);

        spDateToProject.setSelection(0);
        refreshGraphDisplay();
    }

    private void refreshGraphDisplay() {
        if(mQuoteList != null && mQuoteList.size() > 0) {
            String[] labels = QuoteVO.getLabels(mQuoteList);
            int selectedIndex = spDateToProject.getSelectedItemPosition();
            float[][] chartData = QuoteVO.getChartData(mQuoteList, selectedIndex);

            LineSet dataSet = new LineSet(labels, chartData[0]);
            dataSet.setColor(Color.parseColor("#b3b5bb"))
                    .setFill(Color.parseColor("#2d374c"))
                    .setDotsColor(Color.parseColor("#ffc755"))
                    .setDotsRadius(5f)
                    .setThickness(4);

            lcvStockOvertime.reset();
            lcvStockOvertime.addData(dataSet);

            int step = 1;
            int maxClose = (int) QuoteVO.getMaxClose(mQuoteList);

            for (int index = maxClose - 1; index > 1; index--) {
                if (maxClose % index == 0) {
                    step = index;
                    break;
                }
            }

            lcvStockOvertime.setBorderSpacing(Tools.fromDpToPx(30))
                    .setAxisBorderValues(0, maxClose, step)
                    .setYLabels(AxisController.LabelPosition.INSIDE)
                    .setLabelsColor(Color.parseColor("#6a84c3"))
                    .setXAxis(false)
                    .setYAxis(false);

            lcvStockOvertime.show();
        }
    }

    @Override
    public void displayPickedStartDate(Date startDate) {
        String startDateText = DateUtils.sdfQueryDate.format(startDate);
        etStartDate.setText(startDateText);
    }

    @Override
    public void displayPickedEndDate(Date endDate) {
        String endDateText = DateUtils.sdfQueryDate.format(endDate);
        etEndDate.setText(endDateText);
    }

    @OnClick(R.id.et_start_date)
    public void onTapStartDate(View view) {
        String startDate = etStartDate.getText().toString();
        DialogFragment newFragment = DatePickerFragment.newInstance(startDate, DatePickerFragment.DATE_TYPE_START);
        newFragment.show(getSupportFragmentManager(), "et_start_date");
    }

    @OnClick(R.id.et_end_date)
    public void onTapEndDate(View view) {
        String endDate = etEndDate.getText().toString();
        DialogFragment newFragment = DatePickerFragment.newInstance(endDate, DatePickerFragment.DATE_TYPE_END);
        newFragment.show(getSupportFragmentManager(), "et_end_date");
    }

    @OnClick(R.id.btn_update_chart)
    public void onTapUpdateChart(View view) {
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        mPresenter.retrieveStockHistoricalData(startDate, endDate);
        vcLoader.displayLoader();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                null,
                QuoteColumns._ID + " = ? ",
                new String[]{String.valueOf(mRowId)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            viTodayStock.setVisibility(View.VISIBLE);
            viTodayStock.setData(data);
        } else {
            viTodayStock.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
