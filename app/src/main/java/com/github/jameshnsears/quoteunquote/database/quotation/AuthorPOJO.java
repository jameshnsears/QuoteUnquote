package com.github.jameshnsears.quoteunquote.database.quotation;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Locale;

public class AuthorPOJO implements Comparable<AuthorPOJO> {
    @Ignore
    @NonNull
    private static final Collator FINAL_COLLATOR = Collator.getInstance(Locale.ENGLISH);
    @NonNull
    public final String author;

    @Ignore
    @NonNull
    private final CollationKey key;

    @ColumnInfo(name = "QUOTATION_COUNT")
    public int count;

    public AuthorPOJO(@NonNull final String author) {
        this.author = author;
        this.key = FINAL_COLLATOR.getCollationKey(author);
    }

    @Override
    public int compareTo(@NonNull final AuthorPOJO authorPOJO) {
        return this.key.compareTo(authorPOJO.key);
    }
}
