package com.github.jameshnsears.quoteunquote.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

@Dao
@TypeConverters(ContentSelection.class)
public interface CurrentDAO {
    @Insert
    void markAsCurrent(CurrentEntity currentEntity);

    @Query("SELECT COUNT(*) FROM CURRENT WHERE WIDGET_ID = :widgetId")
    int countCurrent(int widgetId);

    @Query("SELECT DIGEST FROM CURRENT WHERE WIDGET_ID = :widgetId")
    String getCurrentDigest(int widgetId);

    @Query("DELETE FROM CURRENT")
    void erase();

    @Query("DELETE FROM CURRENT WHERE WIDGET_ID = :widgetId")
    void erase(int widgetId);

    @Query("DELETE FROM CURRENT WHERE DIGEST = :digest")
    void erase(String digest);
}
