package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udacity.stockhawk.data.Contract.Quote;

import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY_CLOSE;


class DbHelper extends SQLiteOpenHelper {


    private static final String NAME = "StockHawk.db";
    private static final int VERSION = 1;


    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " ("
                + Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Quote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Quote.COLUMN_PRICE + " REAL NOT NULL, "
                + Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_HISTORY_DATE + " TEXT NOT NULL, "
                + Quote.COLUMN_HISTORY_CLOSE + " TEXT NOT NULL, "
                + Quote.COLUMN_HISTORY_MAX_DATES + " TEXT, "
                + Quote.COLUMN_HISTORY_MAX_CLOSE + " TEXT, "
                + Quote.COLUMN_HISTORY_DAILY_DATES + " TEXT, "
                + Quote.COLUMN_HISTORY_DAILY_CLOSE + " TEXT, "
                + Quote.COLUMN_HISTORY_MONTHLY_DATES + " TEXT, "
                + Quote.COLUMN_HISTORY_MONTHLY_CLOSE + " TEXT, "
                + Quote.COLUMN_HISTORY_3YEAR_DATES + " TEXT, "
                + Quote.COLUMN_HISTORY_3YEAR_CLOSE + " TEXT, "
                + "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);
        onCreate(db);
    }
}
