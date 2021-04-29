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

    ContentSelection(@NonNull Integer value) {
        code = value;
    }

    @TypeConverter
    @Nullable
    public static ContentSelection getContentSelection(@NonNull Integer integer) {
        ContentSelection contentSelection = null;
        for (ContentSelection contentSelectionValue : ContentSelection.values()) {
            if (Objects.equals(contentSelectionValue.code, integer)) {
                contentSelection = contentSelectionValue;
                break;
            }
        }
        return contentSelection;
    }

    @TypeConverter
    @NonNull
    public static Integer getContentSelectionInt(@NonNull ContentSelection contentSelection) {
        return contentSelection.code;
    }

    @NonNull
    public Integer getContentSelection() {
        return this.code;
    }
}
