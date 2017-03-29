package com.udacity.stockhawk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.widget.StockEyasWidget;
import com.udacity.stockhawk.widget.WidgetDataProvider;
import com.udacity.stockhawk.widget.WidgetService;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import static android.R.id.message;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.udacity.stockhawk.R.id.symbol;
import static com.udacity.stockhawk.R.string.search;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;


    //bindings to avoid declarations of each view
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.search_button)
    ImageView search;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private Snackbar snackbar;


    private StockAdapter adapter;

    @Override
    public void onClick(String symbol) {

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            ButterKnife.bind(this);
            snackbar = Snackbar
                .make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
            search.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              button(search);
                                          }
                                      }
            );
            //viewholder library
            //stockadapter(context, StockAdapterOnClickHandler)
            adapter = new StockAdapter(this, this);
            //stockadapter in recyclerview
            stockRecyclerView.setAdapter(adapter);
            //layout similar to listview
            stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            //refreshable!
            swipeRefreshLayout.setOnRefreshListener(this);
            //start refresh visual
            swipeRefreshLayout.setRefreshing(true);
            //begin refresh
            onRefresh();
            //sync class
            QuoteSyncJob.initialize(this);
            //call loader by the id
            getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
            //actions to change recyclerview
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                //if switped, remove from prefs
                //not working right atm
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                    PrefUtils.removeStock(MainActivity.this, symbol);
                    getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);

                }
            }).attachToRecyclerView(stockRecyclerView);


    }
    public static void updateAllWidgets(final Context context,
                                        final int layoutResourceId,
                                        final Class< ? extends AppWidgetProvider> appWidgetClass)
    {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, appWidgetClass));

        for (int i = 0; i < appWidgetIds.length; ++i)
        {
            manager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
    }

    //check if network is currently active, returns bool for addstock
    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }


    //implemented from swiperefreshlayout
    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(this);
        //if no network and nothing in the adapter to output
        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            snackbar = Snackbar
                    .make(coordinatorLayout, getString(R.string.error_no_network), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            //if network is not available
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            snackbar = Snackbar
                    .make(coordinatorLayout,  getString(R.string.toast_no_connectivity), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            //if there are no stocks in the preferences
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            snackbar = Snackbar
                    .make(coordinatorLayout, getString(R.string.error_no_stocks), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        } else {

        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    //add stock
    void addStock(String symbol) {
        //string valid and not empty
        if (symbol != null && !symbol.isEmpty()) {
            //if there is a valid netowrk conenction
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                //display error for refreshing
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                 snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }

            StockValidation stockValidation = new StockValidation();
            stockValidation.execute(symbol);
            //add stock to list of abbreviations
            //sync the layout

        }
    }


    public class StockValidation extends AsyncTask<String,Void,Void> {
        boolean validStock = false;
        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, Stock> quotes = null;
                Stock stock = null;
                try {
                    quotes = YahooFinance.get(params);
                     stock = quotes.get(params[0]);
                }catch (StringIndexOutOfBoundsException e){
                    return null;
                }
                StockQuote quote = stock.getQuote();
                if (quote.getPrice() != null) {
                    PrefUtils.addStock(getApplicationContext(), params[0]);
                    validStock=true;
                } else {
                    validStock=false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (validStock){
                QuoteSyncJob.syncImmediately(getApplicationContext());
            swipeRefreshLayout.setRefreshing(false);
                int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), StockEyasWidget.class));
                StockEyasWidget myWidget = new StockEyasWidget();
                myWidget.onUpdate(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),ids);
                RemoteViews widget = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget3x3);

                widget.setRemoteAdapter(R.id.widget_list,
                        new Intent(getApplicationContext(), WidgetService.class));

            }else{
                swipeRefreshLayout.setRefreshing(false);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, getString(R.string.invalid_stock), Snackbar.LENGTH_LONG);
                snackbar.show();

            }
        }
    }


    //menu including the current unit condition
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //menu + change condition of units
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    //Loader handling
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //visual for refreshing ends
        swipeRefreshLayout.setRefreshing(false);
        //if nothing is available, make invis
        if (data.getCount() != 0) {
            if(snackbar.isShown()) {
                snackbar.dismiss();
            }
        }
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //stop refresh visual
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }
    }
