package com.udacity.stockhawk.ui;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.DetailActivity;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.udacity.stockhawk.R.drawable.percent_change_pill_red;
import static com.udacity.stockhawk.R.drawable.percent_change_rect_red;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    //format for absolute condition
    private final DecimalFormat dollarFormatWithPlus;
    //format for current dollar value
    private final DecimalFormat dollarFormat;
    //format for percentage condition
    private final DecimalFormat percentageFormat;
    private Cursor cursor;

    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
        //get instance of US dollars
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+ $");
        dollarFormatWithPlus.setNegativePrefix("- $");

        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+ ");
        percentageFormat.setNegativePrefix("- ");
    }


    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    //get name at cursor position
    String getSymbolAtPosition(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }


    //create the actual viewholder and push it
    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.list_item_wide, parent, false);
        return new StockViewHolder(item);
    }

    //actual binding to each cursor item
    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);



        //get abbrev
        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        //get price
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

        //get both percentages and absolute changes to be switched on the fly
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);



        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        //change color of background based on negative and positive
        if (rawAbsoluteChange > 0) {
            holder.condition.setBackgroundResource(R.drawable.percent_change_rect_green);

//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            SpannableString str1= new SpannableString(change);
//            str1.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.material_green_dark)), 0, 1, 0);
//            builder.append(str1);
//            holder.change.setText(builder, TextView.BufferType.SPANNABLE);
            holder.change.setText(change);
//            builder = new SpannableStringBuilder();
//            str1= new SpannableString(percentage);
//            str1.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.material_green_dark)), 0, 1, 0);
//            builder.append(str1);
//            holder.percentage.setText(builder, TextView.BufferType.SPANNABLE);
                holder.percentage.setText(percentage);

//            holder.arrows.setLayoutParams(new LinearLayout.LayoutParams(holder.change.getLayoutParams().width,holder.change.getLayoutParams().height));
            holder.arrows.setImageDrawable(context.getResources().getDrawable(R.drawable.green_arrow));

        } else {
            holder.condition.setBackgroundResource(percent_change_rect_red);

//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            SpannableString str1= new SpannableString(change);
//            str1.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.material_red_700)), 0, 1, 0);
//            builder.append(str1);
//            holder.change.setText(builder, TextView.BufferType.SPANNABLE);
//            builder = new SpannableStringBuilder();
//            str1= new SpannableString(percentage);
//            str1.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.material_red_700)), 0, 1, 0);
//            builder.append(str1);
//            holder.percentage.setText(builder, TextView.BufferType.SPANNABLE);
            holder.change.setText(change);
            holder.percentage.setText(percentage);
//            holder.arrows.setLayoutParams(new LinearLayout.LayoutParams(holder.change.getLayoutParams().width,holder.change.getLayoutParams().height));
            holder.arrows.setImageDrawable(context.getResources().getDrawable(R.drawable.red_arrow));

        }





        //will be notified to update if this changes





    }

    //get cursor count
    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //name
        @BindView(R.id.symbol)
        TextView symbol;

        //value
        @BindView(R.id.price)
        TextView price;

        //value for condition
        @BindView(R.id.change)
        TextView change;

        //value for condition
        @BindView(R.id.changePercentage)
        TextView percentage;

        @BindView(R.id.item_full_layout)
        View condition;

        @BindView(R.id.img_arrows)
        ImageView arrows;

        //inflated view
        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        //handle for onitemclick, detailactivity
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));
            Timber.d("Symbol clicked: %s",cursor.getString(symbolColumn) );
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("symbol",cursor.getString(symbolColumn));
            Log.d("logs",cursor.getString(symbolColumn));
            context.startActivity(intent);


        }


    }
}
