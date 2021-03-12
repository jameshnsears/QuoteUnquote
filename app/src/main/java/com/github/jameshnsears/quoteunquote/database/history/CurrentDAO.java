package com.github.jameshnsears.quoteunquote.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

@Dao
@TypeConverters({ContentSelection.class})
public interface CurrentDAO {
    @Insert
    void markAsCurrent(CurrentEntity currentEntity);

    @Query("SELECT COUNT(*) FROM CURRENT WHERE WIDGET_ID = :widgetId AND CONTENT_SELECTION = :contentSelection")
    int countCurrent(int widgetId, ContentSelection contentSelection);

    @Query("SELECT DIGEST FROM CURRENT WHERE WIDGET_ID = :widgetId AND CONTENT_SELECTION = :contentSelection")
    String getCurrent(int widgetId, ContentSelection contentSelection);

    @Query("DELETE FROM CURRENT")
    void deleteAll();

    @Query("DELETE FROM CURRENT WHERE WIDGET_ID = :widgetId")
    void deleteAll(int widgetId);

    @Query("DELETE FROM CURRENT WHERE WIDGET_ID = :widgetId AND CONTENT_SELECTION = :contentSelection")
    void deleteAll(int widgetId, ContentSelection contentSelection);
}
