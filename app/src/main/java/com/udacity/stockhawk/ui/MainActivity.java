package com.udacity.stockhawk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

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

    @BindView(R.id.searchkey)
    EditText searchkey;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.empty)
    TextView error;


    private StockAdapter adapter;


    //start: log class
    //todo: detailactivity
    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
    }

    private float dX, dY;
    private boolean ticker = false;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        error = (TextView)findViewById(R.id.empty);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            ButterKnife.bind(this);

            search.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              button(error);
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

    private void updateEmptyView(){
        Log.d("empty","Update empty started");
        if (adapter.getItemCount()==0){
            if(error!=null){
                Log.d("empty","Update empty tv not null");
                int message = R.string.empty_forecast_list;
                if(!networkUp()){
                    message = R.string.empty_forecast_list_no_network;
                }
                error.setText(getString(message));
                Log.d("empty",getString(message));

            }
        }else{
            if(error!=null) {
                error.setVisibility(View.GONE);
            }
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
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
            //if network is not available
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
            error.setVisibility(View.VISIBLE);
            //if there are no stocks in the preferences
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
//            Log.d("empty","good refresh");
//            QuoteSyncJob.syncImmediately(this);
//            getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

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
                //???
                swipeRefreshLayout.setRefreshing(true);
            } else {
                //display error for refreshing
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            //add stock to list of abbreviations
            //sync the layout
            StockValidation stockValidation = new StockValidation();
            stockValidation.execute(symbol);
        }
    }


    public class StockValidation extends AsyncTask<String,Void,Void> {
        boolean validStock = false;
        @Override
        protected Void doInBackground(String... params) {
            try {
                Map<String, Stock> quotes = null;
                quotes = YahooFinance.get(params);
                Stock stock = quotes.get(params[0]);
                StockQuote quote = stock.getQuote();
                if (quote.getPrice() != null) {
                    Log.d("stockfetch", "STOCK ACCEPTED");
                    PrefUtils.addStock(getApplicationContext(), params[0]);
                    validStock=true;
                } else {
                    Log.d("stockfetch", "FALSE STOCK");
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "false stock!", Snackbar.LENGTH_INDEFINITE);

                    snackbar.show();
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
                Log.d("stockfetch", "VALID STOCK SUBMITTED");
                QuoteSyncJob.syncImmediately(getApplicationContext());
            swipeRefreshLayout.setRefreshing(false);

            }else{
                Log.d("stockfetch", "STOCK SUCCESSFULLY EXITED");
                swipeRefreshLayout.setRefreshing(false);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Stock is invalid", Snackbar.LENGTH_LONG);

                snackbar.show();

            }
        }
    }




    //change icon based on condition
    private void setDisplayModeMenuItemIcon(MenuItem item) {

        if (PrefUtils.getDisplayMode(this).equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    //menu including the current unit condition
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
//        MenuItem item = menu.findItem(R.id.action_change_units);
//        setDisplayModeMenuItemIcon(item);
        return true;
    }

    //menu + change condition of units
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_units) {
            //change the current condition of units
            PrefUtils.toggleDisplayMode(this);
            //switch key item icon where the settings would be
            setDisplayModeMenuItemIcon(item);
            //notify the adapter
            adapter.notifyDataSetChanged();
            return true;
        }
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
            error.setVisibility(View.GONE);
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
