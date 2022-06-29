package com.github.jameshnsears.quoteunquote.database.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

@Entity(tableName = "previous",
        indices = {@Index("digest"), @Index({"widget_id", "content_type", "digest"})})
@TypeConverters(ContentSelection.class)
public class PreviousEntity {
    @ColumnInfo(name = "widget_id")
    public final int widgetId;

    @NonNull
    @ColumnInfo(name = "content_type")
    public final ContentSelection contentType;

    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    @PrimaryKey(autoGenerate = true)
    public int navigation;

    public PreviousEntity(
            final int widgetId,
            @NonNull final ContentSelection contentType,
            @NonNull final String digest) {
        this.widgetId = widgetId;
        this.contentType = contentType;
        this.digest = digest;
    }
}
