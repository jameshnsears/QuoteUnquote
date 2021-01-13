package com.github.jameshnsears.quoteunquote.database.history;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class},
        version = 1)
public abstract class AbstractDatabaseHistory extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.db";
    @Nullable
    private static AbstractDatabaseHistory historyDatabase;

    @NonNull
    public static AbstractDatabaseHistory getDatabase(@NonNull final Context context) {
        synchronized (AbstractDatabaseHistory.class) {
            if (historyDatabase == null) {
                historyDatabase = Room.databaseBuilder(context,
                        AbstractDatabaseHistory.class, DATABASE_NAME)
                        .build();
            }
            return historyDatabase;
        }
    }

    @NonNull
    public abstract PreviousDAO contentDAO();

    @NonNull
    public abstract FavouriteDAO favouritesDAO();

    @NonNull
    public abstract ReportedDAO reportedDAO();
}
