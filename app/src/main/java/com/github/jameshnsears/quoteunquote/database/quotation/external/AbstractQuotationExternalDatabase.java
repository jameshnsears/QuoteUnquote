package com.github.jameshnsears.quoteunquote.database.quotation.external;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationDAO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

@Database(entities = QuotationEntity.class, version = 1)
public abstract class AbstractQuotationExternalDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "quotations.external.db";

    @Nullable
    public static AbstractQuotationExternalDatabase quotationExternalDatabase;

    @NonNull
    public static AbstractQuotationExternalDatabase getDatabase(@NonNull Context context) {
        synchronized (AbstractQuotationExternalDatabase.class) {
            if (quotationExternalDatabase == null) {
                quotationExternalDatabase = Room.databaseBuilder(context,
                                AbstractQuotationExternalDatabase.class, DATABASE_NAME)
                        .createFromAsset(DATABASE_NAME)
                        .build();
            }
            return quotationExternalDatabase;
        }
    }

    public abstract QuotationDAO quotationExternalDAO();
}
