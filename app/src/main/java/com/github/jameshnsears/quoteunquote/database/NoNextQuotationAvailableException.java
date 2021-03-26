package com.github.jameshnsears.quoteunquote.database;

import com.github.jameshnsears.quoteunquote.utils.ContentType;

public class NoNextQuotationAvailableException extends Exception {
    public final ContentType contentType;

    public NoNextQuotationAvailableException(final ContentType contentType) {
        super("NoNextQuotationAvailableException: " + contentType.toString());
        this.contentType = contentType;
    }
}
