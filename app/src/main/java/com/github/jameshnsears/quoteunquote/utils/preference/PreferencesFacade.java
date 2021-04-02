package com.github.jameshnsears.quoteunquote.utils.preference;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class PreferencesFacade {
    @NonNull
    public static final String PREFERENCES_FILENAME = "QuoteUnquote";
    protected int widgetId;
    protected final Context applicationContext;
    @Nullable
    protected final PreferenceHelper preferenceHelper;

    public PreferencesFacade(final int theWidgetId, @NonNull final Context applicationContext) {
        this.widgetId = theWidgetId;
        this.applicationContext = applicationContext;
        this.preferenceHelper = new PreferenceHelper(PREFERENCES_FILENAME, applicationContext);
    }

    public static void disable(@NonNull final Context context) {
        PreferenceHelper.empty(PREFERENCES_FILENAME, context);
    }

    public static void delete(@NonNull final Context context, final int widgetId) {
        PreferenceHelper.empty(PREFERENCES_FILENAME, context, widgetId);
    }

    public static int countPreferences(@NonNull final Context context, final int widgetId) {
        return PreferenceHelper.countPreferences(PREFERENCES_FILENAME, context, widgetId);
    }

    @NonNull
    protected String getPreferenceKey(@NonNull final String key) {
        return String.format(Locale.ENGLISH, "%d:%s", widgetId, key);
    }

    @NonNull
    public String getFavouritesLocalCode() {
        return "0:CONTENT_FAVOURITES_LOCAL_CODE";
    }


}
