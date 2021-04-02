package com.github.jameshnsears.quoteunquote.utils.preference;

import android.content.Context;

import androidx.annotation.NonNull;

public class PreferencesMigration extends PreferencesFacade {
    @NonNull
    public static final String PREFERENCES_FILENAME = "QuoteUnquote-Preferences";

    public PreferencesMigration(
            final int widgetId,
            @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }
}
