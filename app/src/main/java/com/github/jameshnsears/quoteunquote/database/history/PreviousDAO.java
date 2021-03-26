package com.github.jameshnsears.quoteunquote.database.history;

import com.github.jameshnsears.quoteunquote.utils.ContentType;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

@Dao
@TypeConverters({ContentType.class})
public interface PreviousDAO {
    @Insert
    void markAsPrevious(PreviousEntity previousEntity);

    @Query("SELECT COUNT(*) FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType")
    int countPrevious(int widgetId, ContentType contentType);

    @Query("SELECT DIGEST FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType ORDER BY NAVIGATION DESC")
    List<String> getPrevious(int widgetId, ContentType contentType);

    @Query("SELECT * FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType ORDER BY NAVIGATION DESC LIMIT 1")
    PreviousEntity getNext(int widgetId, ContentType contentType);

    @Query("DELETE FROM PREVIOUS")
    void deletePrevious();

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId")
    void deletePrevious(int widgetId);

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType")
    void deletePrevious(int widgetId, ContentType contentType);

    @Query("DELETE FROM PREVIOUS WHERE WIDGET_ID = :widgetId AND CONTENT_TYPE = :contentType AND DIGEST = :digest")
    void deletePrevious(int widgetId, ContentType contentType, String digest);
}
