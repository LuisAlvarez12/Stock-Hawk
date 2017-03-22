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

import butterknife.BindView;
import butterknife.ButterKnife;

public class WidgetLeftStock extends AppCompatActivity {

    @BindView(R.id.widget_stock_selection)
    RecyclerView stockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_left_stock);
        ButterKnife.bind(this);

        int positionOfWidgetSection = 0;

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
