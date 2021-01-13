package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import timber.log.Timber;

public class ContentPreferences extends PreferencesFacade {
    public ContentPreferences(final int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public String getContentSelectionAuthorName() {
        return preferenceHelper.getPreferenceString(getPreferenceKey("CONTENT_AUTHOR_NAME"));
    }

    public void setContentSelectionAuthorName(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey("CONTENT_AUTHOR_NAME"), value);
    }

    @NonNull
    public String getContentFavouritesLocalCode() {
        return preferenceHelper.getPreferenceString(getPreferenceKey());
    }

    public void setContentFavouritesLocalCode(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(), value);
    }

    @NonNull
    public String getContentSelectionSearchText() {
        return preferenceHelper.getPreferenceString(getPreferenceKey("CONTENT_SEARCH_TEXT"));
    }

    public void setContentSelectionSearchText(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey("CONTENT_SEARCH_TEXT"), value);
    }

    @NonNull
    public ContentSelection getContentSelection() {
        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey("CONTENT_AUTHOR"), false)) {
            return ContentSelection.AUTHOR;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey("CONTENT_FAVOURITES"), false)) {
            return ContentSelection.FAVOURITES;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey("CONTENT_SEARCH"), false)) {
            return ContentSelection.SEARCH;
        }

        return ContentSelection.ALL;
    }

    public void setContentSelection(@NonNull final ContentSelection contentSelection) {
        switch (contentSelection) {
            case ALL:
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_ALL"), true);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_AUTHOR"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_FAVOURITES"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_SEARCH"), false);
                break;

            case AUTHOR:
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_ALL"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_AUTHOR"), true);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_FAVOURITES"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_SEARCH"), false);
                break;

            case FAVOURITES:
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_ALL"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_AUTHOR"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_FAVOURITES"), true);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_SEARCH"), false);
                break;

            case SEARCH:
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_ALL"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_AUTHOR"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_FAVOURITES"), false);
                preferenceHelper.setPreference(getPreferenceKey("CONTENT_SEARCH"), true);
                break;

            default:
                Timber.e(contentSelection.toString());
                break;
        }
    }
}
