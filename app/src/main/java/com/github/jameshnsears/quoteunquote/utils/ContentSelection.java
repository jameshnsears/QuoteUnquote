package com.github.jameshnsears.quoteunquote.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Objects;

public enum ContentSelection {
    ALL(1),
    FAVOURITES(2),
    AUTHOR(3),
    SEARCH(4);

    @NonNull
    private final Integer code;

    ContentSelection(@NonNull final Integer value) {
        this.code = value;
    }

    @TypeConverter
    @Nullable
    public static ContentSelection getContentSelection(@NonNull final Integer integer) {
        ContentSelection contentSelection = null;
        for (final ContentSelection contentSelectionValue : values()) {
            if (Objects.equals(contentSelectionValue.code, integer)) {
                contentSelection = contentSelectionValue;
                break;
            }
        }
        return contentSelection;
    }

    @TypeConverter
    @NonNull
    public static Integer getContentSelectionInt(@NonNull final ContentSelection contentSelection) {
        return contentSelection.code;
    }

    @NonNull
    public Integer getContentSelection() {
        return code;
    }
}
