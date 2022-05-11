package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class AppearancePreferences extends PreferencesFacade {
    public static final String APPEARANCE_TRANSPARENCY = "APPEARANCE_TRANSPARENCY";
    public static final String APPEARANCE_COLOUR = "APPEARANCE_COLOUR";
    public static final String APPEARANCE_TEXT_FAMILY = "APPEARANCE_TEXT_FAMILY";
    public static final String APPEARANCE_TEXT_STYLE = "APPEARANCE_TEXT_STYLE";
    public static final String APPEARANCE_TEXT_SIZE = "APPEARANCE_TEXT_SIZE";
    public static final String APPEARANCE_TEXT_COLOUR = "APPEARANCE_TEXT_COLOUR";
    public static final String APPEARANCE_TOOLBAR_COLOUR = "APPEARANCE_TOOLBAR_COLOUR";
    public static final String APPEARANCE_TOOLBAR_FIRST = "APPEARANCE_TOOLBAR_FIRST";
    public static final String APPEARANCE_TOOLBAR_PREVIOUS = "APPEARANCE_TOOLBAR_PREVIOUS";
    public static final String APPEARANCE_TOOLBAR_FAVOURITE = "APPEARANCE_TOOLBAR_FAVOURITE";
    public static final String APPEARANCE_TOOLBAR_SHARE = "APPEARANCE_TOOLBAR_SHARE";
    public static final String APPEARANCE_TOOLBAR_RANDOM = "APPEARANCE_TOOLBAR_RANDOM";
    public static final String APPEARANCE_TOOLBAR_SEQUENTIAL = "APPEARANCE_TOOLBAR_SEQUENTIAL";

    public AppearancePreferences(
            int widgetId,
            @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public int getAppearanceTransparency() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TRANSPARENCY));
    }

    public void setAppearanceTransparency(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TRANSPARENCY), value);
    }

    @NonNull
    public String getAppearanceTextFamily() {
        String textFamily = this.preferenceHelper.getPreferenceString(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_FAMILY));
        if (textFamily.equals("")) {
            textFamily = "Sans Serif";
        }
        return textFamily;
    }

    public void setAppearanceTextFamily(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_FAMILY), value);
    }

    @NonNull
    public String getAppearanceTextStyle() {
        String textStyle = this.preferenceHelper.getPreferenceString(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_STYLE));
        if (textStyle.equals("")) {
            textStyle = "Regular";
        }
        return textStyle;
    }

    public void setAppearanceTextStyle(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_STYLE), value);
    }

    @NonNull
    public String getAppearanceColour() {
        String appearanceColour = this.preferenceHelper.getPreferenceString(this.getPreferenceKey(AppearancePreferences.APPEARANCE_COLOUR));
        if (appearanceColour.equals("")) {
            appearanceColour = "#FFF8FD89";
        }
        return appearanceColour;
    }

    public void setAppearanceColour(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_COLOUR), value);
    }

    @NonNull
    public String getAppearanceToolbarColour() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_COLOUR));
    }

    public void setAppearanceToolbarColour(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_COLOUR), value);
    }

    @NonNull
    public String getAppearanceTextColour() {
        String textColour = this.preferenceHelper.getPreferenceString(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_COLOUR));
        if (textColour.equals("")) {
            textColour = "#FF000000";
        }
        return textColour;
    }

    public void setAppearanceTextColour(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_COLOUR), value);
    }

    public int getAppearanceTextSize() {
        int textSize = this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_SIZE));
        if (textSize == -1) {
            textSize = 16;
        }
        return textSize;
    }

    public void setAppearanceTextSize(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TEXT_SIZE), value);
    }

    public boolean getAppearanceToolbarFirst() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_FIRST), false);
    }

    public void setAppearanceToolbarFirst(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_FIRST), value);
    }

    public boolean getAppearanceToolbarPrevious() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_PREVIOUS), true);
    }

    public void setAppearanceToolbarPrevious(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_PREVIOUS), value);
    }

    public boolean getAppearanceToolbarFavourite() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_FAVOURITE), true);
    }

    public void setAppearanceToolbarFavourite(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_FAVOURITE), value);
    }

    public boolean getAppearanceToolbarShare() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_SHARE), true);
    }

    public void setAppearanceToolbarShare(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_SHARE), value);
    }

    public boolean getAppearanceToolbarRandom() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_RANDOM), true);
    }

    public void setAppearanceToolbarRandom(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_RANDOM), value);
    }

    public boolean getAppearanceToolbarSequential() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_SEQUENTIAL), false);
    }

    public void setAppearanceToolbarSequential(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(AppearancePreferences.APPEARANCE_TOOLBAR_SEQUENTIAL), value);
    }
}
