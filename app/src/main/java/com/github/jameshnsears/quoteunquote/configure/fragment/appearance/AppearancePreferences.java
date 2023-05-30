package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class AppearancePreferences extends PreferencesFacade {
    public static final String APPEARANCE_COLOUR = "APPEARANCE_COLOUR";
    public static final String APPEARANCE_TRANSPARENCY = "APPEARANCE_TRANSPARENCY";

    public static final String APPEARANCE_TEXT_STYLE = "APPEARANCE_TEXT_STYLE";
    public static final String APPEARANCE_TEXT_SIZE = "APPEARANCE_TEXT_SIZE";
    public static final String APPEARANCE_TEXT_FORCE_ITALIC_REGULAR
            = "APPEARANCE_TEXT_FORCE_ITALIC_REGULAR";
    public static final String APPEARANCE_TEXT_CENTER
            = "APPEARANCE_TEXT_CENTER";

    public static final String APPEARANCE_QUOTATION_TEXT_COLOUR = "APPEARANCE_TEXT_COLOUR";
    public static final String APPEARANCE_QUOTATION_TEXT_FAMILY = "APPEARANCE_TEXT_FAMILY";

    public static final String APPEARANCE_AUTHOR_TEXT_COLOUR = "APPEARANCE_AUTHOR_TEXT_COLOUR";
    public static final String APPEARANCE_AUTHOR_TEXT_SIZE = "APPEARANCE_AUTHOR_TEXT_SIZE";
    public static final String APPEARANCE_AUTHOR_TEXT_HIDE = "APPEARANCE_AUTHOR_TEXT_HIDE";

    public static final String APPEARANCE_POSITION_TEXT_COLOUR = "APPEARANCE_POSITION_TEXT_COLOUR";
    public static final String APPEARANCE_POSITION_TEXT_SIZE = "APPEARANCE_POSITION_TEXT_SIZE";
    public static final String APPEARANCE_POSITION_TEXT_HIDE = "APPEARANCE_POSITION_TEXT_HIDE";

    public static final String APPEARANCE_TOOLBAR_COLOUR = "APPEARANCE_TOOLBAR_COLOUR";
    public static final String APPEARANCE_TOOLBAR_FIRST = "APPEARANCE_TOOLBAR_FIRST";
    public static final String APPEARANCE_TOOLBAR_PREVIOUS = "APPEARANCE_TOOLBAR_PREVIOUS";
    public static final String APPEARANCE_TOOLBAR_FAVOURITE = "APPEARANCE_TOOLBAR_FAVOURITE";
    public static final String APPEARANCE_TOOLBAR_SHARE = "APPEARANCE_TOOLBAR_SHARE";
    public static final String APPEARANCE_TOOLBAR_SHARE_NO_SOURCE = "APPEARANCE_TOOLBAR_SHARE_NO_SOURCE";
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

    @NonNull
    public String getAppearancePositionTextColour() {
        String textColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_POSITION_TEXT_COLOUR));
        if (textColour.equals("")) {
            textColour = DEFAULT_COLOUR;
        }
        return textColour;
    }

    public void setAppearancePositionTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_POSITION_TEXT_COLOUR), value);
    }

    public int getAppearancePositionTextSize() {
        int textSize = preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_POSITION_TEXT_SIZE));
        if (textSize == -1) {
            textSize = 16;
        }
        return textSize;
    }

    public void setAppearancePositionTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_POSITION_TEXT_SIZE), value);
    }

    public boolean getAppearancePositionTextHide() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_POSITION_TEXT_HIDE), false);
    }

    public void setAppearancePositionTextHide(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_POSITION_TEXT_HIDE), value);
    }

    @NonNull
    public String getAppearanceAuthorTextColour() {
        String textColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_COLOUR));
        if (textColour.equals("")) {
            textColour = DEFAULT_COLOUR;
        }
        return textColour;
    }

    public void setAppearanceAuthorTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_COLOUR), value);
    }

    public int getAppearanceAuthorTextSize() {
        int textSize = preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_SIZE));
        if (textSize == -1) {
            textSize = 16;
        }
        return textSize;
    }

    public void setAppearanceAuthorTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_SIZE), value);
    }

    public boolean getAppearanceAuthorTextHide() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_HIDE), false);
    }

    public void setAppearanceAuthorTextHide(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_AUTHOR_TEXT_HIDE), value);
    }

    @NonNull
    public String getAppearanceQuotationTextColour() {
        String textColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_QUOTATION_TEXT_COLOUR));
        if (textColour.equals("")) {
            textColour = DEFAULT_COLOUR;
        }
        return textColour;
    }

    public void setAppearanceQuotationTextColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_QUOTATION_TEXT_COLOUR), value);
    }

    @NonNull
    public String getAppearanceTextFamily() {
        String textFamily = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_QUOTATION_TEXT_FAMILY));
        if (textFamily.equals("")) {
            textFamily = "Sans Serif";
        }
        return textFamily;
    }

    public void setAppearanceTextFamily(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_QUOTATION_TEXT_FAMILY), value);
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

    public int getAppearanceQuotationTextSize() {
        int textSize = preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TEXT_SIZE));
        if (textSize == -1) {
            textSize = 16;
        }
        return textSize;
    }

    public void setAppearanceTextForceItalicRegular(final boolean value) {
        preferenceHelper.setPreference(
                getPreferenceKey(APPEARANCE_TEXT_FORCE_ITALIC_REGULAR), value);
    }

    public boolean getAppearanceTextForceItalicRegular() {
        return preferenceHelper.getPreferenceBoolean(
                getPreferenceKey(APPEARANCE_TEXT_FORCE_ITALIC_REGULAR), true);
    }

    public void setAppearanceTextCenter(final boolean value) {
        preferenceHelper.setPreference(
                getPreferenceKey(APPEARANCE_TEXT_CENTER), value);
    }

    public boolean getAppearanceTextCenter() {
        return preferenceHelper.getPreferenceBoolean(
                getPreferenceKey(APPEARANCE_TEXT_CENTER), true);
    }

    public void setAppearanceQuotationTextSize(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TEXT_SIZE), value);
    }

    public boolean getAppearanceToolbarJump() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_JUMP), true);
    }

    public void setAppearanceToolbarJump(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_JUMP), value);
    }

    public boolean getAppearanceForceFollowSystemTheme() {
        return preferenceHelper.getPreferenceBoolean("0:APPEARANCE_FORCE_FOLLOW_SYSTEM_THEME", false);
    }

    public void setAppearanceForceFollowSystemTheme(final boolean value) {
        preferenceHelper.setPreference("0:APPEARANCE_FORCE_FOLLOW_SYSTEM_THEME", value);
    }

    public int getAppearanceTransparency() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(APPEARANCE_TRANSPARENCY));
    }

    public void setAppearanceTransparency(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TRANSPARENCY), value);
    }

    @NonNull
    public String getAppearanceColour() {
        String appearanceColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_COLOUR));
        if (appearanceColour.equals("")) {
            appearanceColour = DEFAULT_COLOUR_BACKGROUND;

        }
        return appearanceColour;
    }

    public static String DEFAULT_COLOUR_BACKGROUND = "#FFFFFFFF";

    public void setAppearanceColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_COLOUR), value);
    }

    @NonNull
    public String getAppearanceToolbarColour() {
        String toolbarColour = preferenceHelper.getPreferenceString(getPreferenceKey(APPEARANCE_TOOLBAR_COLOUR));
        if (toolbarColour.equals("")) {
            toolbarColour = DEFAULT_COLOUR;
        }
        return toolbarColour;
    }

    public static String DEFAULT_COLOUR = "#FF000000";

    public void setAppearanceToolbarColour(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_COLOUR), value);
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
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE), false);
    }

    public void setAppearanceToolbarShare(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE), value);
    }

    public boolean getAppearanceToolbarShareNoSource() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE_NO_SOURCE), false);
    }

    public void setAppearanceToolbarShareNoSource(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(APPEARANCE_TOOLBAR_SHARE_NO_SOURCE), value);
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
