package com.github.jameshnsears.quoteunquote.db.q;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = QuotationEntity.class, version = 1)
public abstract class ExternalDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "quotations.external.db";

    @Nullable
    public static ExternalDatabase externalDatabase;

    @NonNull
    public static ExternalDatabase getDatabase(@NonNull final Context context) {
        synchronized (ExternalDatabase.class) {
            if (null == externalDatabase) {
                ExternalDatabase.externalDatabase = Room.databaseBuilder(context,
                                ExternalDatabase.class, ExternalDatabase.DATABASE_NAME)
                        .createFromAsset(ExternalDatabase.DATABASE_NAME)
                        .build();
            }
            return ExternalDatabase.externalDatabase;
        }
    }

    public abstract QuotationDAO quotationExternalDAO();
}
