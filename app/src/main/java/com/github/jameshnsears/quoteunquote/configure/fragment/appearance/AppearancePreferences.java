package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class AppearancePreferences extends PreferencesFacade {
    public static final String APPEARANCE_COLOUR = "APPEARANCE_COLOUR";
    public static final String APPEARANCE_TRANSPARENCY = "APPEARANCE_TRANSPARENCY";
    public static final String APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR
            = "APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR";
    public static final String APPEARANCE_TEXT_COLOUR = "APPEARANCE_TEXT_COLOUR";
    public static final String APPEARANCE_TEXT_FAMILY = "APPEARANCE_TEXT_FAMILY";
    public static final String APPEARANCE_TEXT_STYLE = "APPEARANCE_TEXT_STYLE";
    public static final String APPEARANCE_TEXT_SIZE = "APPEARANCE_TEXT_SIZE";
    public static final String APPEARANCE_TOOLBAR_COLOUR = "APPEARANCE_TOOLBAR_COLOUR";
    public static final String APPEARANCE_TOOLBAR_FIRST = "APPEARANCE_TOOLBAR_FIRST";
    public static final String APPEARANCE_TOOLBAR_PREVIOUS = "APPEARANCE_TOOLBAR_PREVIOUS";
    public static final String APPEARANCE_TOOLBAR_FAVOURITE = "APPEARANCE_TOOLBAR_FAVOURITE";
    public static final String APPEARANCE_TOOLBAR_SHARE = "APPEARANCE_TOOLBAR_SHARE";
    public static final String APPEARANCE_TOOLBAR_JUMP = "APPEARANCE_TOOLBAR_JUMP";
    public static final String APPEARANCE_TOOLBAR_RANDOM = "APPEARANCE_TOOLBAR_RANDOM";
    public static final String APPEARANCE_TOOLBAR_SEQUENTIAL = "APPEARANCE_TOOLBAR_SEQUENTIAL";

    public AppearancePreferences(
            @NonNull final Context applicationContext) {
        super(applicationContext);
    }

    public AppearancePreferences(
            final int widgetId,
            @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getAppearanceToolbarJump() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_JUMP), false);
    }

    public void setAppearanceToolbarJump(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_JUMP), value);
    }

    public boolean getAppearanceRemoveSpaceAboveToolbar() {
        return preferenceHelper.getPreferenceBoolean("0:APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR", false);
    }

    public void setAppearanceRemoveSpaceAboveToolbar(final boolean value) {
        preferenceHelper.setPreference("0:APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR", value);
    }

    public int getAppearanceTransparency() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TRANSPARENCY));
    }

    public void setAppearanceTransparency(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TRANSPARENCY), value);
    }

    @NonNull
    public String getAppearanceTextFamily() {
        String textFamily = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TEXT_FAMILY));
        if (textFamily.equals("")) {
            textFamily = "Sans Serif";
        }
        return textFamily;
    }

    public void setAppearanceTextFamily(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_FAMILY), value);
    }

    @NonNull
    public String getAppearanceTextStyle() {
        String textStyle = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TEXT_STYLE));
        if (textStyle.equals("")) {
            textStyle = "Regular";
        }
        return textStyle;
    }

    public void setAppearanceTextStyle(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_STYLE), value);
    }

    @NonNull
    public String getAppearanceColour() {
        String appearanceColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_COLOUR));
        if (appearanceColour.equals("")) {
            appearanceColour = "#FFF8FD89";

        }
        return appearanceColour;
    }

    public void setAppearanceColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_COLOUR), value);
    }

    @NonNull
    public String getAppearanceToolbarColour() {
        String toolbarCololur = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TOOLBAR_COLOUR));
        if (toolbarCololur.equals("")) {
            toolbarCololur = "#FF000000";
        }
        return toolbarCololur;
    }

    public void setAppearanceToolbarColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_COLOUR), value);
    }

    @NonNull
    public String getAppearanceTextColour() {
        String textColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TEXT_COLOUR));
        if (textColour.equals("")) {
            textColour = "#FF000000";
        }
        return textColour;
    }

    public void setAppearanceTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_COLOUR), value);
    }

    public int getAppearanceTextSize() {
        int textSize = preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TEXT_SIZE));
        if (textSize == -1) {
            textSize = 16;
        }
        return textSize;
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

    public boolean getAppearanceToolbarFavourite() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_FAVOURITE), true);
    }

    public void setAppearanceToolbarFavourite(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_FAVOURITE), value);
    }

    public boolean getAppearanceToolbarShare() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE), true);
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
}
