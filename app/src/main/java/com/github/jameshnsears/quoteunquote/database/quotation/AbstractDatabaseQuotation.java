package com.github.jameshnsears.quoteunquote.database.quotation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.jameshnsears.quoteunquote.BuildConfig;

@Database(
        entities = {QuotationEntity.class},
        version = 1)
public abstract class AbstractDatabaseQuotation extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "quotations.db";

    @Nullable
    private static AbstractDatabaseQuotation quotationDatabase;

    @NonNull
    public static AbstractDatabaseQuotation getDatabase(@NonNull final Context context) {
        synchronized (AbstractDatabaseQuotation.class) {
            if (quotationDatabase == null) {
                quotationDatabase = Room.databaseBuilder(context,
                        AbstractDatabaseQuotation.class, DATABASE_NAME)
                        .createFromAsset(DATABASE_NAME)
                        .build();
            }
            return quotationDatabase;
        }
    }

    public abstract QuotationDAO quotationsDAO();
}
