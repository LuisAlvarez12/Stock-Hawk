package com.udacity.stockhawk.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.SparkLineAdapter.SparkLineAdapter;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by luisalvarez on 3/18/17.
 */

public class DetailFragment extends Fragment {

    private Cursor symbolObject;
    private float[] retArray;
    private String[] datesArray;
    private String[] args = new String[1];

    private SparkLineAdapter sparkLineAdapter;

    private String[] datesTemp;
    private float[] closeTemp;
    private boolean beenScrubbed = false;
    private String lastScrubbedValue = "";



    @BindView(R.id.tab_layout1)
    TabLayout tabLayout;

    @BindView(R.id.sparkview)
    SparkView lineGraph;

    @BindView(R.id.tv_sparkview)
    TextView graphLabel;

    @BindView(R.id.tv_detail_percentage)
    TextView tvPercentageIndicator;

    @BindView(R.id.tv_detail_actual)
    TextView tvActualIndicator;

//    @BindView(R.id.tv_price)
//    TextView tv_price;

    @BindView(R.id.icon_article)
    ImageView icon_Newspaper;




    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this,rootView);

//        YoYo.with(Techniques.RollIn)
//                .duration(2000)
//                .repeat(1)
//                .playOn(rootView.findViewById(R.id.tittles));
        tabLayout.setSelectedTabIndicatorColor((getResources().getColor(getActivity().getIntent().getIntExtra("color",R.color.material_green_700))));

        CreateInitCursor();
        lineGraph.setLineColor(getResources().getColor(getActivity().getIntent().getIntExtra("color",R.color.material_green_700)));

        //init tabs
        tabLayout.addTab(tabLayout.newTab().setText("D"));tabLayout.addTab(tabLayout.newTab().setText("W"));
        tabLayout.addTab(tabLayout.newTab().setText("M"));tabLayout.addTab(tabLayout.newTab().setText("3 Y"));
        tabLayout.addTab(tabLayout.newTab().setText("Max"));tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        return rootView;
    }

    private float[] cursorToAdapterSelection(String dates, String closings) {
        String[] historyClosings = closings.split(",");
        retArray = new float[historyClosings.length];
        int j = 0;
        for (int i = retArray.length - 1; i >= 0; i--) {
            retArray[j] = Float.parseFloat(historyClosings[i]);
            j++;
        }
        datesArray = dates.split(",");
        return retArray;
    }

    private void PrintAllValues2(){
        int j=0;
        Log.d("printall2",j++ +" : symbol: "+symbolObject.getString(Contract.Quote.POSITION_SYMBOL));
        Log.d("printall2",j++ +" : price "+symbolObject.getString(Contract.Quote.POSITION_PRICE));
        Log.d("printall2",j++ +" : monthly close -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_CLOSE));
        Log.d("printall2",j++ +" : monthly dates -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_DATES));
        Log.d("printall2",j++ +" : 3year close -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_3YEAR_CLOSE));
        Log.d("printall2",j++ +" : 3year dates -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_3YEAR_DATES));
        Log.d("printall2",j++ +" : daily dates -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_DATES));
        Log.d("printall2",j++ +" : daily close -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_CLOSE));
        Log.d("printall2",j++ +" : max close -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_MAX_CLOSE));
        Log.d("printall2",j++ +" : max dates -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_MAX_DATES));
        Log.d("printall2",j++ +" : weekly dates -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_DATE));
        Log.d("printall2",j++ +" : weekly close -"+symbolObject.getString(Contract.Quote.POSITION_HISTORY_CLOSE));
    }

    private void CreateInitCursor() {
        args[0] = getActivity().getIntent().getStringExtra("symbol");
        List contractlist = Contract.Quote.QUOTE_COLUMNS;
        String[] stringArray = Arrays.copyOf(contractlist.toArray(), contractlist.toArray().length, String[].class);
        symbolObject = getActivity().getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?", args, Contract.Quote.COLUMN_SYMBOL);
        if (symbolObject != null) {
            symbolObject.moveToFirst();
        }
        tvPercentageIndicator.setText(symbolObject.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE)+"%");
        tvActualIndicator.setText("$"+symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
//        tv_price.setText("$"+symbolObject.getString(Contract.Quote.POSITION_PRICE));
        Syncer fetchHistoricalData = new Syncer();
        fetchHistoricalData.execute();
    }

    public class Syncer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if(symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_CLOSE)!=null){
            }else {
                QuoteSyncJob.initFullGraphValues(
                        getActivity(),
                        symbolObject.getString(Contract.Quote.POSITION_SYMBOL),
                        symbolObject);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            refreshCursor();
            sparkLineAdapter = new SparkLineAdapter(cursorToAdapterSelection(
                    symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_DATES),
                    symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_CLOSE)));
            lineGraph.setAdapter(sparkLineAdapter);
            datesArray = symbolObject.getString(Contract.Quote.POSITION_HISTORY_DATE).split(",");
            InitListeners(graphLabel);
        }

        private void refreshCursor() {
            symbolObject = getActivity().getContentResolver().query(
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    "symbol=?", args
                    , Contract.Quote.COLUMN_SYMBOL);
            symbolObject.moveToFirst();
        }
    }

    private void InitListeners(final TextView graphLabel) {

        icon_Newspaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.google.com/search?q="+symbolObject.getString(Contract.Quote.POSITION_SYMBOL)+"&tbm=nws";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        lineGraph.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if (value == null && beenScrubbed) {
                    graphLabel.setText(lastScrubbedValue);
                } else if (value == null) {
                    graphLabel.setText("1 Year \n");
                } else {
                    String val =
                            QuoteSyncJob.getDate(
                                    Long.parseLong(
                                            datesArray[(datesArray.length - 1) - sparkLineAdapter.getPosition()]),
                                    "MM/dd/yyyy")
                                    + "\n" + value;
                    graphLabel.setText(val);
                    lastScrubbedValue = val;
                    beenScrubbed = true;

                }
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Syncer async = new Syncer();
                switch (tab.getPosition()) {
                    case 0:
                        sparkLineAdapter = new SparkLineAdapter(
                                cursorToAdapterSelection(symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_DATES),
                                        symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_CLOSE)));
                        lineGraph.setAdapter(sparkLineAdapter);
                        break;
                    case 1:
                        sparkLineAdapter = new SparkLineAdapter(
                                cursorToAdapterSelection(symbolObject.getString(Contract.Quote.POSITION_HISTORY_DATE),
                                        symbolObject.getString(Contract.Quote.POSITION_HISTORY_CLOSE)));
                        lineGraph.setAdapter(sparkLineAdapter);
                        break;
                    case 2:
                        sparkLineAdapter = new SparkLineAdapter(
                                cursorToAdapterSelection(symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_DATES),
                                        symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_CLOSE)));
                        lineGraph.setAdapter(sparkLineAdapter);
                        break;
                    case 3:
                        sparkLineAdapter = new SparkLineAdapter(
                                cursorToAdapterSelection(symbolObject.getString(Contract.Quote.POSITION_HISTORY_3YEAR_DATES),
                                        symbolObject.getString(Contract.Quote.POSITION_HISTORY_3YEAR_CLOSE)));
                        lineGraph.setAdapter(sparkLineAdapter);
                        break;
                    case 4:
                        sparkLineAdapter = new SparkLineAdapter(
                                cursorToAdapterSelection(symbolObject.getString(Contract.Quote.POSITION_HISTORY_MAX_DATES),
                                        symbolObject.getString(Contract.Quote.POSITION_HISTORY_MAX_CLOSE)));
                        lineGraph.setAdapter(sparkLineAdapter);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    }

