package com.udacity.stockhawk.SparkLineAdapter;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.widget.StockEyasWidget;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Created by luisalvarez on 3/19/17.
 */

public class SymbolAdapter extends RecyclerView.Adapter<SymbolAdapter.ViewHolder> {

    private Cursor items;
    private Context context;
    private int itemLayout;
    private int positionWidget =0;

    public SymbolAdapter(Context activity,Cursor items, int itemLayout,int p) {
        this.items = items;
        this.itemLayout = itemLayout;
        this.context = activity;
        this.positionWidget = p;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }
    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        items.moveToPosition(position);
        holder.text.setText(items.getString(Contract.Quote.POSITION_SYMBOL));
        holder.price.setText(items.getString(Contract.Quote.POSITION_PRICE));
        if(Float.parseFloat(items.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE))>=0){
            holder.icon.setImageResource(R.drawable.green_arrow);
        }else{
            holder.icon.setImageResource(R.drawable.red_arrow);
        }
    }

    @Override public int getItemCount() {
        return items.getCount();
    }

      class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView text;
        public ImageView icon;
        public TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.symbol_name);
            price = (TextView) itemView.findViewById(R.id.symbol_price);
            icon = (ImageView)itemView.findViewById(R.id.icon_holder);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            items.moveToPosition(adapterPosition);
            PrefUtils.setStockForWidget(context,items.getString(Contract.Quote.POSITION_SYMBOL),positionWidget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, StockEyasWidget.class));
                if (appWidgetIds.length > 0) {
                    new StockEyasWidget().onUpdate(context, appWidgetManager, appWidgetIds);
                    Log.d("tussle","Functioning!");
                    ((Activity) context).onBackPressed();
                }else{
                    Log.d("tussle","failure!");

                }

        }
    }
}