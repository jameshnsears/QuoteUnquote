package com.github.jameshnsears.quoteunquote.database.history.external;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.jameshnsears.quoteunquote.database.history.CurrentDAO;
import com.github.jameshnsears.quoteunquote.database.history.CurrentEntity;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteDAO;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.database.history.PreviousDAO;
import com.github.jameshnsears.quoteunquote.database.history.PreviousEntity;
import com.github.jameshnsears.quoteunquote.database.history.ReportedDAO;
import com.github.jameshnsears.quoteunquote.database.history.ReportedEntity;

import timber.log.Timber;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class, CurrentEntity.class},
        version = 1)
public abstract class AbstractHistoryExternalDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.external.db";

    @Nullable
    public static AbstractHistoryExternalDatabase historyExternalDatabase;

    @NonNull
    public static AbstractHistoryExternalDatabase getDatabase(@NonNull final Context context) {
        synchronized (AbstractHistoryExternalDatabase.class) {
            Timber.d("%b", historyExternalDatabase == null);
            if (historyExternalDatabase == null) {
                historyExternalDatabase = Room.databaseBuilder(context,
                                AbstractHistoryExternalDatabase.class, DATABASE_NAME)
                        .build();
            }

            return historyExternalDatabase;
        }
    }

    public abstract PreviousDAO previousExternalDAO();

    public abstract FavouriteDAO favouritesExternalDAO();

    public abstract ReportedDAO reportedExternalDAO();

    public abstract CurrentDAO currentExternalDAO();
}
