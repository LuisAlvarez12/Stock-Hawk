package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.DetailActivity;

import java.util.ArrayList;
import java.util.List;

import static android.R.style.Widget;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Context mContext = null;
    Cursor cursor = null;
    String KEY_COLOR = null;
    String KEY_SYMBOL = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        cursor = mContext.getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
        KEY_COLOR = mContext.getResources().getString(R.string.color);
        KEY_SYMBOL = mContext.getResources().getString(R.string.symbol);
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_collections_list_item);
        view.setTextViewText(R.id.collection_symbol_price, cursorPrice(position));
        view.setTextViewText(R.id.collection_symbol_name,cursorSymbol(position));
        view.setInt(R.id.collection_condition_holder,mContext.getResources().getString(R.string.setBackgroundColor),mContext.getResources().getColor(R.color.material_green_700));
        int color = changeConditionColor(position, view);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(KEY_COLOR,color);
        fillInIntent.putExtra(KEY_SYMBOL,cursorSymbol(position));
        view.setOnClickFillInIntent(R.id.onClick, fillInIntent);



        return view;
    }

    private int changeConditionColor(int p, RemoteViews v){
        cursor.moveToPosition(p);
        int color = 0;
        if(Float.parseFloat(cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE))>=0){
            v.setInt(R.id.collection_condition_holder, mContext.getResources().getString(R.string.setBackgroundColor),mContext.getResources().getColor(R.color.material_green_700));
            color = R.color.material_green_700;
        }else{
            v.setInt(R.id.collection_condition_holder, mContext.getResources().getString(R.string.setBackgroundColor),mContext.getResources().getColor(R.color.material_red_700));
            color = R.color.material_red_700;
        }
        return color;
    }

    private String cursorSymbol(int position) {
         cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    private String cursorPrice(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_PRICE);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {
    }

}