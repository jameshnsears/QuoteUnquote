package com.github.jameshnsears.quoteunquote.database.quotation;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "quotations", primaryKeys = {"author", "quotation"})
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

    public QuotationEntity(final String digest, final String author, final String quotation) {
        this.author = author;
        this.quotation = quotation;
        this.digest = digest;
    }

    @Override
    public String toString() {
        return String.format("digest=%s; author=%s", digest, author);
    }

    public String theQuotation() {
        return String.format("%s%n%n - %s", quotation, author);
    }
}