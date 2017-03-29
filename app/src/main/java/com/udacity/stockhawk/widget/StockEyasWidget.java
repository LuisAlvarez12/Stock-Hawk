package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;

import yahoofinance.Stock;

/**
 * Implementation of App Widget functionality.
 */
public class StockEyasWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        QuoteSyncJob.syncImmediately(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget3x3);
        setRemoteAdapter(context, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int i = 0; i < appWidgetIds.length; i++){
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget3x3);

            Intent startActivityIntent = new Intent(context, DetailActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.widget_list, startActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);

//            if (PrefUtils.isNetworkAvailable(context)) {
//                updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
//            }else{
//                Intent toApp = new Intent(context, MainActivity.class);
//                context.startActivity(toApp);
//            }
        }
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, WidgetService.class));
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

