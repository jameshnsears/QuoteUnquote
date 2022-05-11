package com.github.jameshnsears.quoteunquote.database.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favourite")
public class FavouriteEntity {
    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    @PrimaryKey(autoGenerate = true)
    public int navigation;

    public FavouriteEntity(@NonNull final String digest) {
        this.digest = digest;
    }
}
