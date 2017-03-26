package com.udacity.stockhawk.widget;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.SparkLineAdapter.SymbolAdapter;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WidgetStockSelection extends AppCompatActivity {

    @BindView(R.id.widget_stock_selection)
    RecyclerView stockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_stock_selection);
        ButterKnife.bind(this);
        if(PrefUtils.isNetworkAvailable(this))
        QuoteSyncJob.syncImmediately(this);

        int positionOfWidgetSection = 1;

        Cursor symbolObject = getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,null
                , Contract.Quote.COLUMN_SYMBOL);
        SymbolAdapter symbols = new SymbolAdapter(this,symbolObject,R.layout.symbol_list_item,positionOfWidgetSection);
        stockList.setLayoutManager(new LinearLayoutManager(this));
        stockList.setAdapter(symbols);
    }
}