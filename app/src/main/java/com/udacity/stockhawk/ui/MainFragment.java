package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.AddStockDialog;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockAdapter;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import static com.udacity.stockhawk.R.string.search;

/**
 * Created by luisalvarez on 3/18/17.
 */

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;


    //bindings to avoid declarations of each view

    private RecyclerView stockRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    TextView error;

    private StockAdapter adapter;

    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
    }

    private float dX, dY;
    private boolean ticker = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);
        stockRecyclerView =(RecyclerView)rootview.findViewById(R.id.recycler_view);
        swipeRefreshLayout=(SwipeRefreshLayout)rootview.findViewById(R.id.swipe_refresh);
//        search.setOnClickListener(new View.OnClickListener() {
//                                      @Override
//                                      public void onClick(View v) {
//                                          button(error);
//                                      }
//                                  }
//        );
        //viewholder library
        //stockadapter(context, StockAdapterOnClickHandler)
        adapter = new StockAdapter(getActivity(), this);
        //stockadapter in recyclerview
        stockRecyclerView.setAdapter(adapter);
        //layout similar to listview
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //refreshable!
        swipeRefreshLayout.setOnRefreshListener(this);
        //start refresh visual
        swipeRefreshLayout.setRefreshing(true);
        //begin refresh
        onRefresh();
        //sync class
        QuoteSyncJob.initialize(getActivity());
        //call loader by the id
        getLoaderManager().initLoader(STOCK_LOADER, null,(android.app.LoaderManager.LoaderCallbacks<Cursor>) this);
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
                PrefUtils.removeStock(getActivity(), symbol);
                getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);
        return rootview;
    }

    //check if network is currently active, returns bool for addstock
    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void slideToBottom(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }


    //implemented from swiperefreshlayout
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(getActivity());
        //if no network and nothing in the adapter to output
        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);

            //if network is not available
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();

            //if there are no stocks in the preferences
        } else if (PrefUtils.getStocks(getActivity()).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            //error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getActivity().getFragmentManager(), "StockDialogFragment");
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
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
            //add stock to list of abbreviations
            //sync the layout
            StockValidation stockValidation = new StockValidation();
            stockValidation.execute(symbol);
        }
    }

    public class StockValidation extends AsyncTask<String, Void, Void> {
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
                    PrefUtils.addStock(getActivity().getApplicationContext(), params[0]);
                    validStock = true;
                } else {
                    Log.d("stockfetch", "FALSE STOCK");
                    validStock = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (validStock) {
                Log.d("stockfetch", "VALID STOCK SUBMITTED");

                QuoteSyncJob.syncImmediately(getActivity().getApplicationContext());
                swipeRefreshLayout.setRefreshing(false);

            } else {
                Log.d("stockfetch", "STOCK SUCCESSFULLY EXITED");

                swipeRefreshLayout.setRefreshing(false);

            }
        }

    }

    //change icon based on condition
    private void setDisplayModeMenuItemIcon(MenuItem item) {

        if (PrefUtils.getDisplayMode(getActivity()).equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    //menu including the current unit condition
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
//        MenuItem item = menu.findItem(R.id.action_change_units);
//        setDisplayModeMenuItemIcon(item);
        return true;
    }

    //menu + change condition of units
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_units) {
            //change the current condition of units
            PrefUtils.toggleDisplayMode(getActivity());
            //switch key item icon where the settings would be
            setDisplayModeMenuItemIcon(item);
            //notify the adapter
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //visual for refreshing ends
        swipeRefreshLayout.setRefreshing(false);
        //if nothing is available, make invis
        if (data.getCount() != 0) {
//            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        //stop refresh visual
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }
}

