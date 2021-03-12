package com.github.jameshnsears.quoteunquote.database.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

@Entity(tableName = "current")
@TypeConverters({ContentSelection.class})
public class CurrentEntity {
    @ColumnInfo(name = "widget_id")
    public final int widgetId;

    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    @NonNull
    @ColumnInfo(name = "content_selection")
    public final ContentSelection contentSelection;

    @PrimaryKey(autoGenerate = true)
    public int navigation;

    public CurrentEntity(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest) {
        this.widgetId = widgetId;
        this.contentSelection = contentSelection;
        this.digest = digest;
    }
}
