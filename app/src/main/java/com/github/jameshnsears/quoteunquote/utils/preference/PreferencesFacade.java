package com.github.jameshnsears.quoteunquote.utils.preference;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class PreferencesFacade {
    @NonNull
    public static final String PREFERENCES_FILENAME = "QuoteUnquote";
    private final int widgetId;
    @Nullable
    protected PreferenceHelper preferenceHelper;

    public PreferencesFacade(final int theWidgetId, @NonNull final Context applicationContext) {
        this.widgetId = theWidgetId;
        this.preferenceHelper = new PreferenceHelper(PREFERENCES_FILENAME, applicationContext);
    }

    public static void empty(@NonNull final Context context) {
        PreferenceHelper.empty(PREFERENCES_FILENAME, context);
    }

    public static void empty(@NonNull final Context context, final int widgetId) {
        PreferenceHelper.empty(PREFERENCES_FILENAME, context, widgetId);
    }

    @NonNull
    protected String getPreferenceKey(@NonNull final String key) {
        return String.format(Locale.ENGLISH, "%d:%s", widgetId, key);
    }

    @NonNull
    protected String getPreferenceKey() {
        return String.format(Locale.ENGLISH, "0:CONTENT_FAVOURITES_LOCAL_CODE");
    }
}
