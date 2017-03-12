package com.udacity.stockhawk;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        SparkView lineGraph = (SparkView)findViewById(R.id.sparkview);
        getStockHistory();




    }


    private float[] getStockHistory(){
        List contractlist = Contract.Quote.QUOTE_COLUMNS;

        String[] stringArray = Arrays.copyOf(contractlist.toArray(), contractlist.toArray().length, String[].class);
        for(int i = 0;i<stringArray.length;i++){
            Log.d("logs",stringArray[i]);
        }
        String[] args = {getIntent().getStringExtra("symbol")};


        Cursor cursor = getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?",args, Contract.Quote.COLUMN_SYMBOL);

        if(cursor!=null){
            cursor.moveToFirst();
            String oneLineHistory =cursor.getString(Contract.Quote.POSITION_HISTORY);
            String[] all = oneLineHistory.split("[\\r\\n]+");
            String[] all2 = oneLineHistory.split(",");
            int j=0;
            for (String x:all){
                
                Log.d("array",j+"|||| " );
                j++;
            }
            j=0;
            for (String x:all2){
                j++;
                if(j%2!=0) {
                    Log.d("array2", j + "|||| " + x);
                }
            }

            float[] retChart;



        }
        return null;
    }

}
