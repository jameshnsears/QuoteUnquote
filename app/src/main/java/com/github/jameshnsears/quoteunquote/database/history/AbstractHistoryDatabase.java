package com.github.jameshnsears.quoteunquote.database.history;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class},
        version = 1)
public abstract class AbstractHistoryDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "history.db";

    private static AbstractHistoryDatabase historyDatabase;

    public static AbstractHistoryDatabase getDatabase(final Context context) {
        synchronized (AbstractHistoryDatabase.class) {
            if (historyDatabase == null) {
                historyDatabase = Room.databaseBuilder(context,
                        AbstractHistoryDatabase.class, DATABASE_NAME)
                        .build();
            }
            return historyDatabase;
        }
    }

    public abstract PreviousDAO contentDAO();

    public abstract FavouriteDAO favouritesDAO();

    public abstract ReportedDAO reportedDAO();
}