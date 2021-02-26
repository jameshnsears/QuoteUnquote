package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class AppearancePreferences extends PreferencesFacade {
    private static String appearanceTransparency = "APPEARANCE_TRANSPARENCY";
    private static String appearanceTextColour = "APPEARANCE_TEXT_COLOUR";
    private static String appearanceTextSize = "APPEARANCE_TEXT_SIZE";
    private static String appearanceToolbarFirst = "APPEARANCE_TOOLBAR_FIRST";
    private static String appearanceToolbarPrevious = "APPEARANCE_TOOLBAR_PREVIOUS";
    private static String appearanceToolbarReport = "APPEARANCE_TOOLBAR_REPORT";
    private static String appearanceToolbarFavourite = "APPEARANCE_TOOLBAR_FAVOURITE";
    private static String appearanceToolbarShare = "APPEARANCE_TOOLBAR_SHARE";
    private static String appearanceToolbarRandom = "APPEARANCE_TOOLBAR_RANDOM";
    private static String appearanceToolbarSequential = "APPEARANCE_TOOLBAR_SEQUENTIAL";

    public AppearancePreferences(
            final int widgetId,
            @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public int getAppearanceTransparency() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(appearanceTransparency));
    }

    public void setAppearanceTransparency(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceTransparency), value);
    }

    @NonNull
    public String getAppearanceTextColour() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(appearanceTextColour));
    }

    public void setAppearanceTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceTextColour), value);
    }

    public int getAppearanceTextSize() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(appearanceTextSize));
    }

    public void setAppearanceTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceTextSize), value);
    }

    public boolean getAppearanceToolbarFirst() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarFirst), false);
    }

    public void setAppearanceToolbarFirst(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarFirst), value);
    }

    public boolean getAppearanceToolbarPrevious() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarPrevious), true);
    }

    public void setAppearanceToolbarPrevious(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarPrevious), value);
    }

    public boolean getAppearanceToolbarReport() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarReport), false);
    }

    public void setAppearanceToolbarReport(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarReport), value);
    }

    public boolean getAppearanceToolbarFavourite() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarFavourite), true);
    }

    public void setAppearanceToolbarFavourite(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarFavourite), value);
    }

    public boolean getAppearanceToolbarShare() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarShare), false);
    }

    public void setAppearanceToolbarShare(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarShare), value);
    }

    public boolean getAppearanceToolbarRandom() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarRandom), true);
    }

    public void setAppearanceToolbarRandom(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarRandom), value);
    }

    public boolean getAppearanceToolbarSequential() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(appearanceToolbarSequential), false);
    }

    public void setAppearanceToolbarSequential(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(appearanceToolbarSequential), value);
    }
}
