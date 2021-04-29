package com.github.jameshnsears.quoteunquote.database.quotation;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(tableName = "quotations",
        indices = {@Index("digest"), @Index({"digest", "author"})},
        primaryKeys = {"author", "quotation"})
public class QuotationEntity {
    @NonNull
    @ColumnInfo(name = "author", collate = ColumnInfo.NOCASE)
    public final String author;

    @NonNull
    @ColumnInfo(name = "quotation", collate = ColumnInfo.NOCASE)
    public final String quotation;

    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;

    public QuotationEntity(
            @NonNull String digest,
            @NonNull String author,
            @NonNull String quotation) {
        this.author = author;
        this.quotation = quotation;
        this.digest = digest;
    }

    @NonNull
    public String theQuotation() {
        return String.format("%s%n%n%s ", this.quotation, this.author);
    }
}
