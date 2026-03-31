package com.github.jameshnsears.quoteunquote.db.q;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.jameshnsears.quoteunquote.BuildConfig;

@Database(entities = {QuotationEntity.class}, version = 53)
public abstract class QuotationDatabase extends RoomDatabase {
    @Nullable
    public static QuotationDatabase quotationDatabase;

    @NonNull
    public static QuotationDatabase getDatabase(@NonNull Context context) {
        synchronized (QuotationDatabase.class) {
            if (null == quotationDatabase) {
                QuotationDatabase.quotationDatabase = Room.databaseBuilder(context,
                                QuotationDatabase.class, BuildConfig.DATABASE_QUOTATIONS)
                        .createFromAsset(BuildConfig.DATABASE_QUOTATIONS)
                        // indexes added
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return QuotationDatabase.quotationDatabase;
        }
    }

    public abstract QuotationDAO quotationDAO();
}
