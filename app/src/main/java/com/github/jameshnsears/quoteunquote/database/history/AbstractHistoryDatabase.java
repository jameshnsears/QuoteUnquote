package com.github.jameshnsears.quoteunquote.database.history;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.jameshnsears.quoteunquote.BuildConfig;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class, CurrentEntity.class},
        version = 2)
public abstract class AbstractHistoryDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.db";
    @Nullable
    private static AbstractHistoryDatabase historyDatabase;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `current` (`widget_id` INTEGER NOT NULL, `digest` TEXT NOT NULL, PRIMARY KEY(`widget_id`))");
        }
    };

    @NonNull
    public static AbstractHistoryDatabase getDatabase(@NonNull final Context context) {
        synchronized (AbstractHistoryDatabase.class) {
            if (historyDatabase == null) {
                if (BuildConfig.DEBUG) {
                    historyDatabase = Room.databaseBuilder(context,
                            AbstractHistoryDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                } else {
                    historyDatabase = Room.databaseBuilder(context,
                            AbstractHistoryDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
            return historyDatabase;
        }
    }

    @NonNull
    public abstract PreviousDAO previousDAO();

    @NonNull
    public abstract FavouriteDAO favouritesDAO();

    @NonNull
    public abstract ReportedDAO reportedDAO();

    @NonNull
    public abstract CurrentDAO currentDAO();
}
