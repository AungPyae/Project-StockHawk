package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.persistent.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.utils.NetworkUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyStocksActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private Intent mServiceIntent;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Cursor mCursor;

    @Bind(R.id.lbl_empty)
    TextView tvEmpty;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.rv_stocks)
    RecyclerView rvStocks;

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeContainer;

    private final BroadcastReceiver mInvalidStockInputReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String stockInput = intent.getStringExtra(StockTaskService.PARAM_STOCK_INPUT_NAME);
            new AlertDialog.Builder(MyStocksActivity.this)
                    .setMessage(getString(R.string.format_invalid_stock_label, stockInput))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stocks);
        ButterKnife.bind(this, this);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (NetworkUtils.getInstance().isOnline()) {
                startService(mServiceIntent);
            }
        }

        mCursorAdapter = new QuoteCursorAdapter(this);

        rvStocks.setLayoutManager(new LinearLayoutManager(this));
        rvStocks.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        if (mCursor.moveToPosition(position)) {
                            String stockSymbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                            int rowId = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns._ID));
                            Intent intentToLineGraph = LineGraphActivity.newIntent(stockSymbol, rowId);
                            startActivity(intentToLineGraph);
                        }
                    }
                }));
        rvStocks.setAdapter(mCursorAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.getInstance().isOnline()) {
                    new MaterialDialog.Builder(MyStocksActivity.this).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString()}, null);
                                    if (cursor != null && cursor.getCount() != 0) {
                                        Toast toast =
                                                Toast.makeText(MyStocksActivity.this, getString(R.string.stock_already_saved),
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("symbol", input.toString());
                                        startService(mServiceIntent);
                                    }

                                    if(cursor != null)
                                        cursor.close();
                                }
                            })
                            .show();
                } else {
                    showDialogMsg(getString(R.string.network_toast));
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvStocks);

        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);

        if (NetworkUtils.getInstance().isOnline()) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mInvalidStockInputReceiver, new IntentFilter(StockTaskService.INVALID_STOCK_INPUT));
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mInvalidStockInputReceiver);
    }

    public void showDialogMsg(String msg) {
        new AlertDialog.Builder(MyStocksActivity.this)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    /*
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        //restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                null,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);

            Snackbar.make(fab, getString(R.string.stock_data_has_been_refreshed), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }

        if (!NetworkUtils.getInstance().isOnline()) {
            if (data.getCount() > 0) {
                showDialogMsg(getString(R.string.no_internet_outdated_data));
            } else {
                showDialogMsg(getString(R.string.no_internet_empty_data));
            }
        }


        if (data != null && data.getCount() > 0) {
            tvEmpty.setVisibility(View.INVISIBLE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }

        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }

        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        if (NetworkUtils.getInstance().isOnline()) {
            startService(mServiceIntent);
        } else {
            showDialogMsg(getString(R.string.network_toast));
        }
    }
}
