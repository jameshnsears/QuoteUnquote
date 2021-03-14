package com.github.jameshnsears.quoteunquote.database.history;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import timber.log.Timber;

@Database(
        entities = {PreviousEntity.class, FavouriteEntity.class, ReportedEntity.class, CurrentEntity.class},
        version = 2)
public abstract class AbstractDatabaseHistory extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.db";
    @Nullable
    private static AbstractDatabaseHistory historyDatabase;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // RENAME keyword not yet present in SQLite Android version
            database.execSQL("CREATE TABLE `previous_new` (`widget_id` INTEGER NOT NULL, `content_selection` INTEGER NOT NULL, `digest` TEXT NOT NULL, `navigation` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)");
            database.execSQL("INSERT INTO `previous_new` (`widget_id`, `content_selection`, `digest`) SELECT `widget_id`, `content_type`, `digest` FROM `previous` ORDER BY `navigation` ASC");
            database.execSQL("DROP TABLE `previous`");
            database.execSQL("ALTER TABLE `previous_new` RENAME TO `previous`");

            database.execSQL("CREATE TABLE `current` (`widget_id` INTEGER NOT NULL, `digest` TEXT NOT NULL, `content_selection` INTEGER NOT NULL, `navigation` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)");
        }
    };

    @NonNull
    public static AbstractDatabaseHistory getDatabase(@NonNull final Context context) {
        synchronized (AbstractDatabaseHistory.class) {
            if (historyDatabase == null) {
                historyDatabase = Room.databaseBuilder(context,
                        AbstractDatabaseHistory.class, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build();
            }
            return historyDatabase;
        }
    }

    @NonNull
    public abstract PreviousDAO previousDAO();

    @NonNull
    public abstract FavouritesDAO favouritesDAO();

    @NonNull
    public abstract ReportedDAO reportedDAO();

    @NonNull
    public abstract CurrentDAO currentDAO();
}
