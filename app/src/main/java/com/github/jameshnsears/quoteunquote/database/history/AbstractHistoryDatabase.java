package com.github.jameshnsears.quoteunquote.database.history;

import android.content.Context;

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
public abstract class AbstractHistoryDatabase extends RoomDatabase {
    @NonNull
    public static final String DATABASE_NAME = "history.db";
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Timber.d(DATABASE_NAME);
            database.execSQL("CREATE TABLE IF NOT EXISTS `current` (`widget_id` INTEGER NOT NULL, `digest` TEXT NOT NULL, PRIMARY KEY(`widget_id`))");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_previous_digest` ON `previous` (`digest`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_previous_widget_id_content_type_digest` ON `previous` (`widget_id`, `content_type`, `digest`)");
        }
    };
    @Nullable
    public static AbstractHistoryDatabase historyDatabase;

    @NonNull
    public static AbstractHistoryDatabase getDatabase(@NonNull final Context context) {
        synchronized (AbstractHistoryDatabase.class) {
            Timber.d("%b", historyDatabase == null);
            if (historyDatabase == null) {
                historyDatabase = Room.databaseBuilder(context,
                                AbstractHistoryDatabase.class, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .build();
            }

            return historyDatabase;
        }
    }

    public abstract PreviousDAO previousDAO();

    public abstract FavouriteDAO favouritesDAO();

    public abstract ReportedDAO reportedDAO();

    public abstract CurrentDAO currentDAO();
}
