package com.github.jameshnsears.quoteunquote.database.history;

import com.github.jameshnsears.quoteunquote.utils.ContentType;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "previous")
@TypeConverters({ContentType.class})
public class PreviousEntity {
    @NonNull
    @ColumnInfo(name = "widget_id")
    public final int widgetId;

    @NonNull
    @ColumnInfo(name = "content_type")
    public final ContentType contentType;

    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    @PrimaryKey(autoGenerate = true)
    public int navigation;

    public PreviousEntity(final int widgetId, final ContentType contentType, final String digest) {
        this.widgetId = widgetId;
        this.contentType = contentType;
        this.digest = digest;
    }
}
