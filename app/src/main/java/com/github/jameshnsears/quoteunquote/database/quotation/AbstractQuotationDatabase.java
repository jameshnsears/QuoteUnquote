package com.github.jameshnsears.quoteunquote.database.quotation;

import android.content.Context;

import com.github.jameshnsears.quoteunquote.BuildConfig;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {QuotationEntity.class},
        version = 1)
public abstract class AbstractQuotationDatabase extends RoomDatabase {
    private static AbstractQuotationDatabase quotationDatabase;

    public static AbstractQuotationDatabase getDatabase(final Context context) {
        String dbName = "quotations.db.dev";

        if (BuildConfig.USE_PROD_DB) {
            dbName = "quotations.db.prod";
        }

        synchronized (AbstractQuotationDatabase.class) {
            if (quotationDatabase == null) {
                quotationDatabase = Room.databaseBuilder(context,
                        AbstractQuotationDatabase.class, dbName)
                        .createFromAsset(dbName)
                        .build();
            }
            return quotationDatabase;
        }
    }

    public abstract QuotationDAO quotationsDAO();
}