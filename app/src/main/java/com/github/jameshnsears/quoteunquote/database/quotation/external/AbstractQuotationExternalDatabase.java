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
    public static AbstractQuotationExternalDatabase getDatabase(@NonNull final Context context) {
        synchronized (AbstractQuotationExternalDatabase.class) {
            if (null == quotationExternalDatabase) {
                AbstractQuotationExternalDatabase.quotationExternalDatabase = Room.databaseBuilder(context,
                                AbstractQuotationExternalDatabase.class, AbstractQuotationExternalDatabase.DATABASE_NAME)
                        .createFromAsset(AbstractQuotationExternalDatabase.DATABASE_NAME)
                        .build();
            }
            return AbstractQuotationExternalDatabase.quotationExternalDatabase;
        }
    }

    public abstract QuotationDAO quotationExternalDAO();
}
