package com.github.jameshnsears.quoteunquote.db.h;

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
