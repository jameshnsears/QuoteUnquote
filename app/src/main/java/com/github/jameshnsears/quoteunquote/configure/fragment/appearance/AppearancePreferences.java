package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class AppearancePreferences extends PreferencesFacade {
    public AppearancePreferences(
            final int widgetId,
            @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public int getAppearanceTransparency() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey("APPEARANCE_TRANSPARENCY"));
    }

    public void setAppearanceTransparency(final int value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TRANSPARENCY"), value);
    }

    @NonNull
    public String getAppearanceTextColour() {
        return preferenceHelper.getPreferenceString(getPreferenceKey("APPEARANCE_TEXT_COLOUR"));
    }

    public void setAppearanceTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TEXT_COLOUR"), value);
    }

    public int getAppearanceTextSize() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey("APPEARANCE_TEXT_SIZE"));
    }

    public void setAppearanceTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TEXT_SIZE"), value);
    }

    public boolean getAppearanceToolbarFirst() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_FIRST"), false);
    }

    public void setAppearanceToolbarFirst(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_FIRST"), value);
    }

    public boolean getAppearanceToolbarPrevious() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_PREVIOUS"), true);
    }

    public void setAppearanceToolbarPrevious(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_PREVIOUS"), value);
    }

    public boolean getAppearanceToolbarReport() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_REPORT"), true);
    }

    public void setAppearanceToolbarReport(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_REPORT"), value);
    }

    public boolean getAppearanceToolbarFavourite() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_FAVOURITE"), true);
    }

    public void setAppearanceToolbarFavourite(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_FAVOURITE"), value);
    }

    public boolean getAppearanceToolbarShare() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_SHARE"), true);
    }

    public void setAppearanceToolbarShare(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_SHARE"), value);
    }

    public boolean getAppearanceToolbarRandom() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_RANDOM"), true);
    }

    public void setAppearanceToolbarRandom(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_RANDOM"), value);
    }

    public boolean getAppearanceToolbarSequential() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey("APPEARANCE_TOOLBAR_SEQUENTIAL"), false);
    }

    public void setAppearanceToolbarSequential(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey("APPEARANCE_TOOLBAR_SEQUENTIAL"), value);
    }
}
