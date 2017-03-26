package com.udacity.stockhawk.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.robinhood.spark.SparkView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.SparkLineAdapter.SparkLineAdapter;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Float.parseFloat;

/**
 * Created by luisalvarez on 3/18/17.
 */

public class DetailFragment extends android.support.v4.app.Fragment {

    private Cursor symbolObject;
    private float[] retArray;
    private String[] datesArray;
    private String[] args = new String[1];

    private SparkLineAdapter sparkLineAdapter;

    private String[] datesTemp;
    private float[] closeTemp;
    private boolean beenScrubbed = false;
    private String lastScrubbedValue = "";


    @BindView(R.id.coordinator_detail)
    CoordinatorLayout coordinator_detail;

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

    @BindView(R.id.tv_detail)
    TextView tv_highLowThirty;

    @BindView(R.id.news_cnn)
    TextView news_cnn;

    @BindView(R.id.news_google)
    TextView news_google;

    @BindView(R.id.news_nasdaq)
    TextView news_nasdaq;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingtoolbar;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

//High and low textview setup
    private String getHighLowThirtyDays(){
        String[] listOfDailyCloses= symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_CLOSE).split(",");
        String[] listOfDailyDates = symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_DATES).split(",");
        float highest = 0;
        int index=0;
        for(int i =0;i<listOfDailyCloses.length;i++){
            if(parseFloat(listOfDailyCloses[i])>highest){
                highest = parseFloat(listOfDailyCloses[i]);
                index=i;
            }else if(highest==0){
               highest = Float.parseFloat(listOfDailyCloses[i]);
                index=i;
            }
        }
        return getActivity().getResources().getString(R.string.dolla_sign)+highest + "\n"+ QuoteSyncJob.getDate(
                Long.parseLong(listOfDailyDates[index]), getActivity().getResources().getString(R.string.month_day_year));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        collapsingtoolbar.setContentScrimColor(getResources().getColor(getActivity().getIntent().getIntExtra("color",R.color.material_green_700)));
        toolbar.setBackgroundColor(getResources().getColor(getActivity().getIntent().getIntExtra("color",R.color.material_green_700)));
        appBarLayout.setBackgroundColor(getResources().getColor(getActivity().getIntent().getIntExtra("color",R.color.material_green_700)));

        CreateInitCursor();
        lineGraph.setLineColor(getResources().getColor(getActivity().getIntent().getIntExtra("color", R.color.material_green_700)));

        //init tabs
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.tab_d)));
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.tab_w)));
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.tab_m)));
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.tab_3y)));
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.tab_max)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        return rootView;
    }


    private float[] cursorToAdapterSelection(String dates, String closings) {
        String[] historyClosings = closings.split(",");
        retArray = new float[historyClosings.length];
        int j = 0;
        for (int i = retArray.length - 1; i >= 0; i--) {
            retArray[j] = parseFloat(historyClosings[i]);
            j++;
        }
        datesArray = dates.split(",");
        return retArray;
    }


    //cursor data setting
    private void CreateInitCursor() {
        args[0] = getActivity().getIntent().getStringExtra(getActivity().getResources().getString(R.string.symbol));
        List contractlist = Contract.Quote.QUOTE_COLUMNS;
        String[] stringArray = Arrays.copyOf(contractlist.toArray(), contractlist.toArray().length, String[].class);
        symbolObject = getActivity().getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?", args, Contract.Quote.COLUMN_SYMBOL);

        if (symbolObject != null) {
            symbolObject.moveToFirst();
        }
        collapsingtoolbar.setTitle(symbolObject.getString(Contract.Quote.POSITION_SYMBOL)+getActivity().getResources().getString(R.string.action_bar_spacing)
                +symbolObject.getString(Contract.Quote.POSITION_PRICE));

        tvPercentageIndicator.setText(symbolObject.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE) + "%");
        tvPercentageIndicator.setContentDescription(symbolObject.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE) + " percentage");
        if(symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE).charAt(0)=='-'){
            char[] replaceChar =  symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE).toCharArray();
            tvActualIndicator.setText("-" + symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE).replace('-','$'));
        }else {
            tvActualIndicator.setText(getActivity().getResources().getString(R.string.dolla_sign) + symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        }
        tvActualIndicator.setContentDescription(getActivity().getResources().getString(R.string.dolla_sign) + symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        if(PrefUtils.isNetworkAvailable(getActivity())) {
            Syncer fetchHistoricalData = new Syncer();
            fetchHistoricalData.execute();
        }else{
            Snackbar snackbar = Snackbar
                    .make(coordinator_detail, getActivity().getString(R.string.error_no_network), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            tv_highLowThirty.setText(getActivity().getString(R.string.not_any));
        }
    }



    public class Syncer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (symbolObject.getString(Contract.Quote.POSITION_HISTORY_MONTHLY_CLOSE) != null) {
            } else {
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
            lineGraph.setAdapter(sparkLineAdapter);
            datesArray = symbolObject.getString(Contract.Quote.POSITION_HISTORY_DATE).split(",");
            InitListeners(graphLabel);
            sparkLineAdapter = new SparkLineAdapter(cursorToAdapterSelection(
                    symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_DATES),
                    symbolObject.getString(Contract.Quote.POSITION_HISTORY_DAILY_CLOSE)));
            lineGraph.setAdapter(sparkLineAdapter);
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
            tv_highLowThirty.setText(getHighLowThirtyDays());

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


    //listerners for scrubbing graph and tab select
    private void InitListeners(final TextView graphLabel) {

        lineGraph.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if (value == null && beenScrubbed) {
                    graphLabel.setText(lastScrubbedValue);
                } else if (value == null) {
                    graphLabel.setText("\n");
                } else {
                    String val =
                            QuoteSyncJob.getDate(
                                    Long.parseLong(
                                            datesArray[(datesArray.length - 1) - sparkLineAdapter.getPosition()]),
                                    getActivity().getResources().getString(R.string.month_day_year))
                                    + "\n" + getActivity().getResources().getString(R.string.dolla_sign)+value;
                    graphLabel.setText(val);
                    graphLabel.setContentDescription(val);
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
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        news_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.google.com/search?q=" + symbolObject.getString(Contract.Quote.POSITION_SYMBOL) + "&tbm=nws";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        news_nasdaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.nasdaq.com/symbol/" + symbolObject.getString(Contract.Quote.POSITION_SYMBOL) + "/news-headlines";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        news_cnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://money.cnn.com/quote/news/news.html?symb=" + symbolObject.getString(Contract.Quote.POSITION_SYMBOL);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }



}

