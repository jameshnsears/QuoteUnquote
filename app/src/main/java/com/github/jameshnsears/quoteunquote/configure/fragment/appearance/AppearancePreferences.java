package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import java.util.Map;

import timber.log.Timber;

public class AppearancePreferences extends PreferencesFacade {
    private static final String APPEARANCE_TRANSPARENCY = "APPEARANCE_TRANSPARENCY";
    private static final String APPEARANCE_COLOUR = "APPEARANCE_COLOUR";
    private static final String APPEARANCE_TEXT_FAMILY = "APPEARANCE_TEXT_FAMILY";
    private static final String APPEARANCE_TEXT_STYLE = "APPEARANCE_TEXT_STYLE";
    private static final String APPEARANCE_TEXT_SIZE = "APPEARANCE_TEXT_SIZE";
    private static final String APPEARANCE_TOOLBAR_FIRST = "APPEARANCE_TOOLBAR_FIRST";
    private static final String APPEARANCE_TOOLBAR_PREVIOUS = "APPEARANCE_TOOLBAR_PREVIOUS";
    private static final String APPEARANCE_TOOLBAR_REPORT = "APPEARANCE_TOOLBAR_REPORT";
    private static final String APPEARANCE_TOOLBAR_FAVOURITE = "APPEARANCE_TOOLBAR_FAVOURITE";
    private static final String APPEARANCE_TOOLBAR_SHARE = "APPEARANCE_TOOLBAR_SHARE";
    private static final String APPEARANCE_TOOLBAR_RANDOM = "APPEARANCE_TOOLBAR_RANDOM";
    private static final String APPEARANCE_TOOLBAR_SEQUENTIAL = "APPEARANCE_TOOLBAR_SEQUENTIAL";

    public AppearancePreferences(
            final int widgetId,
            @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public int getAppearanceTransparency() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TRANSPARENCY));
    }

    public void setAppearanceTransparency(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TRANSPARENCY), value);
    }

    @NonNull
    public String getAppearanceTextFamily() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TEXT_FAMILY));
    }

    public void setAppearanceTextFamily(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_FAMILY), value);
    }

    @NonNull
    public String getAppearanceTextStyle() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TEXT_STYLE));
    }

    public void setAppearanceTextStyle(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_STYLE), value);
    }

    @NonNull
    public String getAppearanceColour() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_COLOUR));
    }

    public void setAppearanceColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_COLOUR), value);
    }

    public int getAppearanceTextSize() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TEXT_SIZE));
    }

    public void setAppearanceTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_SIZE), value);
    }

    public boolean getAppearanceToolbarFirst() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_FIRST), false);
    }

    public void setAppearanceToolbarFirst(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_FIRST), value);
    }

    public boolean getAppearanceToolbarPrevious() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_PREVIOUS), true);
    }

    public void setAppearanceToolbarPrevious(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_PREVIOUS), value);
    }

    public boolean getAppearanceToolbarReport() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_REPORT), false);
    }

    public void setAppearanceToolbarReport(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_REPORT), value);
    }

    public boolean getAppearanceToolbarFavourite() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_FAVOURITE), true);
    }

    public void setAppearanceToolbarFavourite(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_FAVOURITE), value);
    }

    public boolean getAppearanceToolbarShare() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE), false);
    }

    public void setAppearanceToolbarShare(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE), value);
    }

    public boolean getAppearanceToolbarRandom() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_RANDOM), true);
    }

    public void setAppearanceToolbarRandom(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_RANDOM), value);
    }

    public boolean getAppearanceToolbarSequential() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_SEQUENTIAL), false);
    }

    public void setAppearanceToolbarSequential(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_SEQUENTIAL), value);
    }

    public void performMigration() {
        final Map<String, ?> sharedPreferenceEntries
                = applicationContext.getSharedPreferences("QuoteUnquote-Preferences", Context.MODE_PRIVATE).getAll();

        for (final Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            widgetId = Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf(":")));

            if (entry.getKey().contains("FragmentAppearance:seekBar")) {
                int seekBar = (Integer) entry.getValue();
                Timber.d("%d: seekBar=%d", widgetId, seekBar);
                setAppearanceTransparency(seekBar);
            }

            if (entry.getKey().contains("FragmentAppearance:spinnerColour")) {
                String spinnerColour = (String) entry.getValue();
                Timber.d("%d: spinnerColour=%s", widgetId, spinnerColour);
                setAppearanceColour(spinnerColour);
            }

            if (entry.getKey().contains("FragmentAppearance:spinnerSize")) {
                int spinnerSize = (Integer) entry.getValue();
                Timber.d("%d: spinnerSize=%d", widgetId, spinnerSize);
                setAppearanceTextSize(spinnerSize);
            }
        }

        setAppearanceTransparency(0);
    }
}
