package com.github.jameshnsears.quoteunquote.database.quotation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.jameshnsears.quoteunquote.BuildConfig;

@Database(entities = QuotationEntity.class, version = 26)
public abstract class AbstractQuotationDatabase extends RoomDatabase {
    @Nullable
    public static AbstractQuotationDatabase quotationDatabase;

    @NonNull
    public static AbstractQuotationDatabase getDatabase(@NonNull final Context context) {
        synchronized (AbstractQuotationDatabase.class) {
            if (quotationDatabase == null) {
                quotationDatabase = Room.databaseBuilder(context,
                        AbstractQuotationDatabase.class, BuildConfig.DATABASE_QUOTATIONS)
                        .createFromAsset(BuildConfig.DATABASE_QUOTATIONS)
                        // indexes added
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return quotationDatabase;
        }
    }

    public abstract QuotationDAO quotationsDAO();
}
