package com.github.jameshnsears.quoteunquote.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.List;

@Dao
@TypeConverters(ContentSelection.class)
public interface PreviousDAO {
    @Insert
    void markAsPrevious(PreviousEntity previousEntity);

    @Query("SELECT COUNT(*) FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType")
    int countPrevious(int widgetId, ContentSelection contentType);

    @Query("SELECT DIGEST FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType ORDER BY NAVIGATION DESC")
    List<String> getPreviousDigests(int widgetId, ContentSelection contentType);

    @Query("SELECT * FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType ORDER BY NAVIGATION DESC LIMIT 1")
    PreviousEntity getPrevious(int widgetId, ContentSelection contentType);

    @Query("DELETE FROM PREVIOUS")
    void erase();

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId")
    void erase(int widgetId);

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType")
    void erase(int widgetId, ContentSelection contentType);

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType AND DIGEST = :digest")
    void erase(int widgetId, ContentSelection contentType, String digest);
}
