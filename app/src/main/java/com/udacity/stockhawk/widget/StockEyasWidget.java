package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.widget.WidgetStockSelection;

import static com.udacity.stockhawk.R.id.symbol;

/**
 * Implementation of App Widget functionality.
 */
public class StockEyasWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        QuoteSyncJob.syncImmediately(context);
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);
        views.setOnClickPendingIntent(R.id.widget_condition_holder, centerClickPendingIntent(context));
        views.setOnClickPendingIntent(R.id.widget_holder_right,rightClickPendingIntent(context) );
        views.setOnClickPendingIntent(R.id.widget_holder_left, leftClickPendingIntent(context));

        String[] selectedStocks = PrefUtils.getStocksForWidget(context);
        initWidgetViews(context, views, selectedStocks);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    private static PendingIntent centerClickPendingIntent( Context cntx){
        Intent intentCenter = new Intent(cntx,WidgetStockSelection.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(cntx, 0, intentCenter, 0);
        return configPendingIntent;
    }

    private static PendingIntent leftClickPendingIntent(Context cntx){
        Intent intentLeft = new Intent(cntx,WidgetLeftStock.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(cntx, 0, intentLeft, 0);
        return configPendingIntent;
    }

    private static PendingIntent rightClickPendingIntent( Context cntx){
        Intent intentRight = new Intent(cntx,WidgetRightStock.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(cntx, 0, intentRight,0);
        return configPendingIntent;
    }


    private static void initWidgetViews(Context context, RemoteViews views, String[] selectedStocks) {
        //left widget view
        Cursor symbolObject = context.getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?", new String[]{selectedStocks[0]}
                , Contract.Quote.COLUMN_SYMBOL);
        symbolObject.moveToFirst();
        views.setTextViewText(R.id.symbol_left,symbolObject.getString(Contract.Quote.POSITION_SYMBOL));
        views.setTextViewText(R.id.price_left,symbolObject.getString(Contract.Quote.POSITION_PRICE));
        float rawAbsoluteChange = Float.parseFloat(symbolObject.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        if(rawAbsoluteChange>0){
            views.setImageViewResource(R.id.img_arrows_left,R.drawable.green_arrow);
        }else{
            views.setImageViewResource(R.id.img_arrows_left,R.drawable.red_arrow);
        }

        //middle (main) widget view
        symbolObject = context.getContentResolver().query(
               Contract.Quote.URI,
               Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
               "symbol=?", new String[]{selectedStocks[1]}
               , Contract.Quote.COLUMN_SYMBOL);

        symbolObject.moveToFirst();
        views.setTextViewText(symbol,symbolObject.getString(Contract.Quote.POSITION_SYMBOL));
        views.setTextViewText(R.id.price,symbolObject.getString(Contract.Quote.POSITION_PRICE));
        rawAbsoluteChange = symbolObject.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        if(rawAbsoluteChange>0){
            views.setInt(R.id.widget_condition_holder, "setBackgroundColor",context.getResources().getColor(R.color.material_green_700));
            views.setImageViewResource(R.id.img_arrows,R.drawable.white_arrow_increase);
        }else{
            views.setInt(R.id.widget_condition_holder, "setBackgroundColor",context.getResources().getColor(R.color.material_red_700));
            views.setImageViewResource(R.id.img_arrows,R.drawable.white_arrow_down);
        }

        //right widget view
        symbolObject = context.getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol=?", new String[]{selectedStocks[2]}
                , Contract.Quote.COLUMN_SYMBOL);
        symbolObject.moveToFirst();
        views.setTextViewText(R.id.symbol_right,symbolObject.getString(Contract.Quote.POSITION_SYMBOL));
        views.setTextViewText(R.id.price_right,symbolObject.getString(Contract.Quote.POSITION_PRICE));
        rawAbsoluteChange = symbolObject.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        if(rawAbsoluteChange>0){
            views.setImageViewResource(R.id.img_arrows_right,R.drawable.green_arrow);
        }else{
            views.setImageViewResource(R.id.img_arrows_right,R.drawable.red_arrow);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            if (PrefUtils.isNetworkAvailable(context)) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }else{
                Intent toApp = new Intent(context, MainActivity.class);
                context.startActivity(toApp);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
