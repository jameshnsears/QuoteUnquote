package com.github.jameshnsears.quoteunquote.utils;

import java.util.Objects;

import androidx.room.TypeConverter;

public enum ContentType {
    ALL(1),
    FAVOURITES(2),
    AUTHOR(3),
    QUOTATION_TEXT(4),
    REPORT(5);

    private final Integer code;

    ContentType(final Integer value) {
        this.code = value;
    }

    @TypeConverter
    public static ContentType getContentType(final Integer integer) {
        for (final ContentType contentType : values()) {
            if (Objects.equals(contentType.code, integer)) {
                return contentType;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer getContentTypeInt(final ContentType contentType) {
        return contentType.code;
    }

    public Integer getContentType() {
        return code;
    }
}
