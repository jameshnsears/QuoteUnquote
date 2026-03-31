package com.github.jameshnsears.quoteunquote.db.h;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import timber.log.Timber;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class, CurrentEntity.class},
        version = 1)
public abstract class HistoryExternalDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.external.db";

    @Nullable
    public static HistoryExternalDatabase historyExternalDatabase;

    @NonNull
    public static HistoryExternalDatabase getDatabase(@NonNull final Context context) {
        synchronized (HistoryExternalDatabase.class) {
            Timber.d("%b", historyExternalDatabase == null);
            if (historyExternalDatabase == null) {
                historyExternalDatabase = Room.databaseBuilder(context,
                                HistoryExternalDatabase.class, DATABASE_NAME)
                        .build();
            }

            return historyExternalDatabase;
        }
    }

    public abstract PreviousDAO previousExternalDAO();

    public abstract FavouriteDAO favouritesExternalDAO();
    public abstract CurrentDAO currentExternalDAO();
}
