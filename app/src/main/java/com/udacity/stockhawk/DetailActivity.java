package com.udacity.stockhawk;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.SparkLineAdapter.SparkLineAdapter;
import com.udacity.stockhawk.data.Contract;

import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private String lastScrubbedValue = "";
    private boolean beenScrubbed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        SparkView lineGraph = (SparkView) findViewById(R.id.sparkview);
        final TextView graphLabel = (TextView) findViewById(R.id.tv_sparkview);
        lineGraph.setAdapter(new SparkLineAdapter(getStockHistory()));
        lineGraph.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if (value == null && beenScrubbed) {
                    graphLabel.setText(lastScrubbedValue);
                } else if (value==null) {
                    graphLabel.setText("1 Year");

                } else {
                    String val = "" + value;
                    graphLabel.setText(val);
                    lastScrubbedValue = val;
                    beenScrubbed = true;

                }
            }
        });

    }


    private float[] getStockHistory() {
        List contractlist = Contract.Quote.QUOTE_COLUMNS;
        String[] stringArray = Arrays.copyOf(contractlist.toArray(), contractlist.toArray().length, String[].class);
        String[] args = {getIntent().getStringExtra("symbol")};
        Cursor cursor = getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?", args, Contract.Quote.COLUMN_SYMBOL);

        if (cursor != null) {
            cursor.moveToFirst();
            String[] historyClosings = cursor.getString(Contract.Quote.POSITION_HISTORY_CLOSE).split(",");
            float[] retArray = new float[historyClosings.length];
            int j = 0;
            for (int i = retArray.length - 1; i >= 0; i--) {
                retArray[j] = Float.parseFloat(historyClosings[i]);
                j++;
            }
            return retArray;
        }
        return null;
    }

}
