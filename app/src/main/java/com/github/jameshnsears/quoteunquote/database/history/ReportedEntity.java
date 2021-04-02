package com.github.jameshnsears.quoteunquote.database.history;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reported")
public class ReportedEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    public ReportedEntity(@NonNull final String digest) {
        this.digest = digest;
    }
}
