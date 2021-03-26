package com.github.jameshnsears.quoteunquote.database.quotation;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class AuthorPOJO implements Comparable<AuthorPOJO> {
    @Ignore
    private static final Collator collator = Collator.getInstance(Locale.ENGLISH);
    public final String author;

    @Ignore
    private final CollationKey key;

    @ColumnInfo(name = "QUOTATION_COUNT")
    public int count;

    AuthorPOJO(final String author) {
        this.author = author;
        this.key = collator.getCollationKey(author);
    }

    @Override
    public int compareTo(final AuthorPOJO authorPOJO) {
        return this.key.compareTo(authorPOJO.key);
    }
}
