package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

    private PrefUtils() {
    }


    // get list of stocks as a set
    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        //get list of default stocks(aapl, fb,msft,yhoo)
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        //set defaults to hashset
        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //if key is not initialized yet, will default to false
        boolean initialized = prefs.getBoolean(initializedKey, false);

        //create the set in the preferences via the hashset upon first time use
        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            //confirms defautl keys have been initialized
            editor.putBoolean(initializedKey, true);
            //put the string hashet into preferences with the key "stocks"
            editor.putStringSet(stocksKey, defaultStocks);
            //confirm
            editor.apply();
            //return the defaultstocks
            return defaultStocks;
        }
        //return whatever is in the hashset
        return prefs.getStringSet(stocksKey, new HashSet<String>());
    }

    //get stocks to refresh
    public static String[] getStocksForWidget(Context context){
        String[] toRetArray = new String[3];
        String key = "stock_widget";
        String key_LeftSideWidget = "widget_left";
        String key_mainSideWidget = "widget_main";
        String key_rightSideWidget = "widget_right";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        toRetArray[0] = preferences.getString(key_LeftSideWidget,context.getResources().getString(R.string.default_stocks_apple));
        toRetArray[1] = preferences.getString(key_mainSideWidget,context.getResources().getString(R.string.default_stocks_facebook));
        toRetArray[2] = preferences.getString(key_rightSideWidget,context.getResources().getString(R.string.default_stocks_microsoft));
        return toRetArray;
    }

    //get stocks to refresh
    public static void setStockForWidget(Context context, String symbolToReturn, int position){

        String key_LeftSideWidget = "widget_left";
        String key_mainSideWidget = "widget_main";
        String key_rightSideWidget = "widget_right";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        switch (position) {
            case 0:
                editor.putString(key_LeftSideWidget, symbolToReturn);
                break;
            case 1:
                editor.putString(key_mainSideWidget,symbolToReturn);
                break;
            case 2:
                editor.putString(key_rightSideWidget,symbolToReturn);
                break;
        }
        editor.apply();
    }

    //add or remove stock from the list
    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        //get stocks from the pref stringset
        Set<String> stocks = getStocks(context);

        //if param was true, add stock otherwise delete
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }
        //confirm changes
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    //add stock via name
    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }
    //remove stock
    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }


    //get the current status of whether mode is using percentage or absolute values
    public static String getDisplayMode(Context context) {
        //key to get current condition
        String key = context.getString(R.string.pref_display_mode_key);
        //default key
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }


    //change the current status of the displaymode whether it is absolute or percentage
    public static void toggleDisplayMode(Context context) {
        //preference for current key
        String key = context.getString(R.string.pref_display_mode_key);
        //dollars
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        //percentage key
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //get current status
        String displayMode = getDisplayMode(context);
        SharedPreferences.Editor editor = prefs.edit();

        //change depending on current condition
        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }
        editor.apply();
    }

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
