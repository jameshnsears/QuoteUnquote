package com.github.jameshnsears.quoteunquote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Locale;
import java.util.Map;

public class Preferences {
    public static final String FRAGMENT_APPEARANCE = "FragmentAppearance";
    public static final String SEEK_BAR = "seekBar";
    public static final String SPINNER_COLOUR = "spinnerColour";
    public static final String SPINNER_SIZE = "spinnerSize";
    public static final String CHECK_BOX_TOOLBAR = "checkBoxDisplayToolbar";
    public static final String FRAGMENT_CONTENT = "FragmentContent";
    public static final String RADIO_BUTTON_ALL = "radioButtonAll";
    public static final String RADIO_BUTTON_FAVOURITES = "radioButtonFavourites";
    public static final String TEXT_VIEW_FAVOURITES_CODE = "textViewFavouritesCode";
    public static final String RADIO_BUTTON_AUTHOR = "radioButtonAuthor";
    public static final String SPINNER_AUTHORS = "spinnerAuthors";
    public static final String RADIO_BUTTON_QUOTATION_TEXT = "radioButtonKeywords";
    public static final String EDIT_TEXT_KEYWORDS = "editTextKeywords";
    public static final String FRAGMENT_EVENT = "FragmentEvent";
    public static final String CHECK_BOX_DEVICE_UNLOCK = "checkBoxDeviceUnlock";
    public static final String CHECK_BOX_DAILY_AT = "checkBoxDailyAt";
    public static final String TIME_PICKER_HOUR = "timePickerDailyAt:hourOfDay";
    public static final String TIME_PICKER_MINUTE = "timePickerDailyAt:minute";
    private static final String LOG_TAG = Preferences.class.getSimpleName();
    private static final String PREFERENCES_FILENAME = "QuoteUnquote-Preferences";
    private final Context context;
    private final int widgetId;

    public Preferences(final int widgetId, final Context context) {
        this.widgetId = widgetId;
        this.context = context;
    }

    private static String getWidgetTagKey(final int widgetId, final String fragment, final String key) {
        return String.format(Locale.ENGLISH, "%d:%s:%s", widgetId, fragment, key);
    }

    public static void removeSharedPreferencesForWidgetId(final Context context, final int widgetId) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        final SharedPreferences.Editor sharedPreferenceEditor = sharedPreferences.edit();

        final Map<String, ?> sharedPreferenceEntries = sharedPreferences.getAll();
        for (final Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            if (entry.getKey().startsWith(String.format(Locale.ENGLISH, "%d:", widgetId))) {
                sharedPreferenceEditor.remove(entry.getKey());
            }
        }

        sharedPreferenceEditor.apply();
    }

    public static void empty(final Context context) {
        Log.d(LOG_TAG, String.format("%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final SharedPreferences sharedPrefs = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.apply();
    }

    public String getSharedPreferenceLocalCode() {
        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(
                Preferences.getWidgetTagKey(0, FRAGMENT_CONTENT, TEXT_VIEW_FAVOURITES_CODE), "");
    }

    public void setSharedPreferenceLocalCode(final String localCode) {
        final SharedPreferences.Editor sharedPrefEditor = getSharedPreferenceEditor().edit();
        sharedPrefEditor.putString(
                Preferences.getWidgetTagKey(0, FRAGMENT_CONTENT, TEXT_VIEW_FAVOURITES_CODE),
                localCode);
        sharedPrefEditor.apply();
    }

    public String getSharedPreferenceString(final String fragment, final String key) {
        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(
                Preferences.getWidgetTagKey(this.widgetId, fragment, key), "");
    }

    public int getSharedPreferenceInt(final String fragment, final String key) {
        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(
                Preferences.getWidgetTagKey(this.widgetId, fragment, key), -1);
    }

    public boolean getSharedPreferenceBoolean(final String fragment, final String key, final boolean defaultValue) {
        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(
                Preferences.getWidgetTagKey(this.widgetId, fragment, key), defaultValue);
    }

    public boolean getSharedPreferenceBoolean(final String fragment, final String key) {
        final SharedPreferences sharedPreferences
                = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(
                Preferences.getWidgetTagKey(this.widgetId, fragment, key), false);
    }

    private SharedPreferences getSharedPreferenceEditor() {
        return this.context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    public void setSharedPreference(final String fragment, final String key, final String value) {
        final SharedPreferences.Editor sharedPrefEditor = getSharedPreferenceEditor().edit();
        sharedPrefEditor.putString(getWidgetTagKey(fragment, key), value);
        sharedPrefEditor.apply();
    }

    private String getWidgetTagKey(final String fragment, final String key) {
        return String.format(Locale.ENGLISH, "%d:%s:%s", widgetId, fragment, key);
    }

    public void setSharedPreference(final String fragment, final String key, final boolean value) {
        final SharedPreferences.Editor sharedPrefEditor = getSharedPreferenceEditor().edit();
        sharedPrefEditor.putBoolean(getWidgetTagKey(fragment, key), value);
        sharedPrefEditor.apply();
    }

    public void setSharedPreference(final String fragment, final String key, final int value) {
        final SharedPreferences.Editor sharedPrefEditor = getSharedPreferenceEditor().edit();
        sharedPrefEditor.putInt(getWidgetTagKey(fragment, key), value);
        sharedPrefEditor.apply();
    }

    public ContentType getSelectedContentType() {
        ContentType contentType;

        if (getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL)) {
            contentType = ContentType.ALL;
        } else if (getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES)) {
            contentType = ContentType.FAVOURITES;
        } else if (getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR)) {
            contentType = ContentType.AUTHOR;
        } else if (getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT)) {
            contentType = ContentType.QUOTATION_TEXT;
        } else {
            contentType = ContentType.ALL;
        }

        return contentType;
    }
}
