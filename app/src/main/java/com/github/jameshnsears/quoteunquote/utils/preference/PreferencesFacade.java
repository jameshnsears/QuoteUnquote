package com.github.jameshnsears.quoteunquote.utils.preference;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class PreferencesFacade {
    @NonNull
    public static final String PREFERENCES_FILENAME = "QuoteUnquote";
    @NonNull
    protected final Context applicationContext;
    @Nullable
    protected final PreferenceHelper preferenceHelper;
    protected int widgetId;

    public PreferencesFacade(int theWidgetId, @NonNull Context applicationContext) {
        widgetId = theWidgetId;
        this.applicationContext = applicationContext;
        preferenceHelper = new PreferenceHelper(PreferencesFacade.PREFERENCES_FILENAME, applicationContext);
    }

    public static void disable(@NonNull Context context) {
        PreferenceHelper.empty(PreferencesFacade.PREFERENCES_FILENAME, context);
    }

    public static void delete(@NonNull Context context, int widgetId) {
        PreferenceHelper.empty(PreferencesFacade.PREFERENCES_FILENAME, context, widgetId);
    }

    public static int countPreferences(@NonNull Context context, int widgetId) {
        return PreferenceHelper.countPreferences(PreferencesFacade.PREFERENCES_FILENAME, context, widgetId);
    }

    @NonNull
    protected String getPreferenceKey(@NonNull String key) {
        return String.format(Locale.ENGLISH, "%d:%s", this.widgetId, key);
    }

    @NonNull
    public String getFavouritesLocalCode() {
        return "0:CONTENT_FAVOURITES_LOCAL_CODE";
    }


}
