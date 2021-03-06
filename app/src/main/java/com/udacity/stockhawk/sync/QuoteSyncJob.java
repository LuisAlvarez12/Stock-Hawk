package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 30;

    public QuoteSyncJob() {
    }

    public static void initFullGraphValues(Context context, String symbol, Cursor currentSymbol) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Quote.COLUMN_SYMBOL, currentSymbol.getString(Contract.Quote.POSITION_SYMBOL));
        contentValues.put(Contract.Quote.COLUMN_PRICE, currentSymbol.getString(Contract.Quote.POSITION_PRICE));
        contentValues.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, currentSymbol.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
        contentValues.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, currentSymbol.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        contentValues.put(Contract.Quote.COLUMN_HISTORY_DATE, currentSymbol.getString(Contract.Quote.POSITION_HISTORY_DATE));
        contentValues.put(Contract.Quote.COLUMN_HISTORY_CLOSE, currentSymbol.getString(Contract.Quote.POSITION_HISTORY_CLOSE));

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        String[] stockArray = {symbol};
        Map<String, Stock> quotes = null;
        try {
            quotes = YahooFinance.get(stockArray);
            ArrayList<ContentValues> quoteCVs = new ArrayList<>();
            Stock stock = quotes.get(symbol);
            StockQuote quote = stock.getQuote();
            List<HistoricalQuote> history;
            String closeMax = "";
            String datesMax = "";
            HistoryFetcher historyFetcher;
            int j;
            for (int i = 0; i < 4; i++) {
                switch (i) {
                    case 0:
                        from = Calendar.getInstance();
                        from.add(Calendar.WEEK_OF_YEAR, -4);
                        history = stock.getHistory(from, to, Interval.DAILY);
                         historyFetcher = new HistoryFetcher(history).invoke();
                        closeMax = historyFetcher.getCloseMax();
                        datesMax = historyFetcher.getDatesMax();
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_DAILY_CLOSE, datesMax);
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_DAILY_DATES, closeMax);
                        break;
                    case 1:
                        from = Calendar.getInstance();
                        from.add(Calendar.YEAR, -1);
                        history = stock.getHistory(from, to, Interval.MONTHLY);
                        historyFetcher = new HistoryFetcher(history).invoke();
                        closeMax = historyFetcher.getCloseMax();
                        datesMax = historyFetcher.getDatesMax();
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_MONTHLY_CLOSE, datesMax);
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_MONTHLY_DATES, closeMax);
                        break;
                    case 2:
                        from.add(Calendar.YEAR, -3);
                        history = stock.getHistory(from, to, Interval.MONTHLY);
                        historyFetcher = new HistoryFetcher(history).invoke();
                        closeMax = historyFetcher.getCloseMax();
                        datesMax = historyFetcher.getDatesMax();
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_3YEAR_CLOSE, datesMax);
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_3YEAR_DATES, closeMax);
                        break;
                    case 3:
                        from = Calendar.getInstance();
                        from.add(Calendar.YEAR, -100);
                        history = stock.getHistory(from, to, Interval.MONTHLY);
                        historyFetcher = new HistoryFetcher(history).invoke();
                        closeMax = historyFetcher.getCloseMax();
                        datesMax = historyFetcher.getDatesMax();
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_MAX_CLOSE, datesMax);
                        contentValues.put(Contract.Quote.COLUMN_HISTORY_MAX_DATES, closeMax);
                        break;
                }

            }
            String[] arg = {currentSymbol.getString(Contract.Quote.POSITION_SYMBOL)};
            context.getContentResolver().update(
                    Contract.Quote.URI,
                    contentValues,
                    Contract.Quote.COLUMN_SYMBOL + "=?",
                    arg
            );


        } catch (IOException e) {

        }

    }


    static void getQuotes(Context context) {

        //
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.MONTH, -6);

        try {

            //get the stored abbrevs
            Set<String> stockPref = PrefUtils.getStocks(context);
            //store copy of stockpref
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            //string array of original abbrevs
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            //if null, exit
            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                String closing="";
                String dates="";
                int i=1;
                for(HistoricalQuote historyItems:history){
                    if(i++==history.size()){
                        dates=dates+historyItems.getDate().getTimeInMillis();
                        closing=closing+historyItems.getClose()+"";
                    }else{
                        dates=dates+historyItems.getDate().getTimeInMillis()+",";
                        closing=closing+historyItems.getClose()+",";
                    }
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY_DATE,dates);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY_CLOSE, closing);


                //convert contentvalues to quotecv
                quoteCVs.add(quoteCV);
            }

            //bulkinsert with arraylist
            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            //?????
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }


    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");

        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


    private static class HistoryLooper {
        private List<HistoricalQuote> history;
        private String closeMax;
        private String datesMax;

        public HistoryLooper(List<HistoricalQuote> history) {
            this.history = history;
        }

        public String getCloseMax() {
            return closeMax;
        }

        public String getDatesMax() {
            return datesMax;
        }

        public HistoryLooper invoke() {
            int j;
            closeMax = "";
            datesMax = "";
            j = history.size() - 1;
            for (HistoricalQuote hQuote : history) {
                if (j == 0) {
                    closeMax = closeMax + hQuote.getClose();
                    datesMax = datesMax + hQuote.getDate().getTimeInMillis();
                } else {
                    closeMax = closeMax + hQuote.getClose() + "!";
                    datesMax = datesMax + hQuote.getDate().getTimeInMillis() + "!";
                }
                j--;
            }
            return this;
        }
    }

    private static class HistoryFetcher {
        private List<HistoricalQuote> history;
        private String closeMax;
        private String datesMax;

        public HistoryFetcher(List<HistoricalQuote> history) {
            this.history = history;
        }

        public String getCloseMax() {
            return closeMax;
        }

        public String getDatesMax() {
            return datesMax;
        }

        public HistoryFetcher invoke() {
            int j;
            closeMax = "";
            datesMax = "";
            j = history.size() - 1;
            for (HistoricalQuote hQuote : history) {
                if (j == 0) {
                    closeMax = closeMax + hQuote.getClose();
                    datesMax = datesMax + hQuote.getDate().getTimeInMillis();
                } else {
                    closeMax = closeMax + hQuote.getClose() + ",";
                    datesMax = datesMax + hQuote.getDate().getTimeInMillis() + ",";
                }
                j--;
            }
            return this;
        }
    }
}
