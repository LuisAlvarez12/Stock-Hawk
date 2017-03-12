package com.udacity.stockhawk.SparkLineAdapter;

import com.robinhood.spark.SparkAdapter;

/**
 * Created by luisalvarez on 3/11/17.
 */

public class SparkLineAdapter extends SparkAdapter {
    private float[] yData;

    public SparkLineAdapter(float[] yData) {
        this.yData = yData;
    }

    @Override
    public int getCount() {
        return yData.length;
    }

    @Override
    public Object getItem(int index) {
        return yData[index];
    }

    @Override
    public float getY(int index) {
        return yData[index];
    }
}