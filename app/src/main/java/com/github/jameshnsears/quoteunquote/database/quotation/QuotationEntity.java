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
    @ColumnInfo(name = "quotation", collate = ColumnInfo.NOCASE)
    public final String quotation;
    @NonNull
    @ColumnInfo(name = "wikipedia", collate = ColumnInfo.NOCASE)
    public final String wikipedia;
    @NonNull
    @ColumnInfo(name = "digest")
    public final String digest;
    @NonNull
    @ColumnInfo(name = "author", collate = ColumnInfo.NOCASE)
    public String author;

    public QuotationEntity(
            @NonNull String digest,
            @NonNull String wikipedia,
            @NonNull String author,
            @NonNull String quotation) {
        this.author = author;
        this.wikipedia = wikipedia;
        this.quotation = quotation;
        this.digest = digest;
    }

    @NonNull
    public String theQuotation() {
        return this.quotation + "\n";
    }

    @NonNull
    public String theAuthor() {
        return this.author + "\n";
    }

    @NonNull
    public String shareQuotationAuthor() {
        return this.quotation + "\n\n" + this.author;
    }

    @NonNull
    public String shareQuotation() {
        return this.quotation;
    }
}
