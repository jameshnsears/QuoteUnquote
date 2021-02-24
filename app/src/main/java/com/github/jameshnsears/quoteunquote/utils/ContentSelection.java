package com.github.jameshnsears.quoteunquote.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Objects;

public enum ContentSelection {
    ALL(1),
    FAVOURITES(2),
    AUTHOR(3),
    SEARCH(4),
    REPORT(5);

    @NonNull
    private final Integer code;

    ContentSelection(@NonNull final Integer value) {
        this.code = value;
    }

    @TypeConverter
    @Nullable
    public static ContentSelection getContentType(@NonNull final Integer integer) {
        ContentSelection contentType = null;
        for (final ContentSelection contentSelection : values()) {
            if (Objects.equals(contentSelection.code, integer)) {
                contentType = contentSelection;
                break;
            }
        }
        return contentType;
    }

    @TypeConverter
    @NonNull
    public static Integer getContentTypeInt(@NonNull final ContentSelection contentSelection) {
        return contentSelection.code;
    }

    @NonNull
    public Integer getContentType() {
        return code;
    }
}
