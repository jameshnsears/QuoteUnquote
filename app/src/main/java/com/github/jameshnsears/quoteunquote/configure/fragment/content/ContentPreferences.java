package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import timber.log.Timber;

public class ContentPreferences extends PreferencesFacade {
    private static String contentAll = "CONTENT_ALL";
    private static String contentAuthor = "CONTENT_AUTHOR";
    private static String contentAuthorName = "CONTENT_AUTHOR_NAME";
    private static String contentFavourites = "CONTENT_FAVOURITES";
    private static String contentSearch = "CONTENT_SEARCH";
    private static String contentSearchText = "CONTENT_SEARCH_TEXT";

    public ContentPreferences(@NonNull final Context applicationContext) {
        super(0, applicationContext);
    }

    public ContentPreferences(final int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public String getContentSelectionAuthorName() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(contentAuthorName));
    }

    public void setContentSelectionAuthorName(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(contentAuthorName), value);
    }

    @NonNull
    public String getContentFavouritesLocalCode() {
        return preferenceHelper.getPreferenceString(getFavouritesLocalCode());
    }

    public void setContentFavouritesLocalCode(@NonNull final String value) {
        preferenceHelper.setPreference(getFavouritesLocalCode(), value);
    }

    @NonNull
    public String getContentSelectionSearchText() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(contentSearchText));
    }

    public void setContentSelectionSearchText(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(contentSearchText), value);
    }

    @NonNull
    public ContentSelection getContentSelection() {
        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(contentAuthor), false)) {
            return ContentSelection.AUTHOR;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(contentFavourites), false)) {
            return ContentSelection.FAVOURITES;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(contentSearch), false)) {
            return ContentSelection.SEARCH;
        }

        return ContentSelection.ALL;
    }

    public void setContentSelection(@NonNull final ContentSelection contentSelection) {
        switch (contentSelection) {
            case ALL:
                preferenceHelper.setPreference(getPreferenceKey(contentAll), true);
                preferenceHelper.setPreference(getPreferenceKey(contentAuthor), false);
                preferenceHelper.setPreference(getPreferenceKey(contentFavourites), false);
                preferenceHelper.setPreference(getPreferenceKey(contentSearch), false);
                break;

            case AUTHOR:
                preferenceHelper.setPreference(getPreferenceKey(contentAll), false);
                preferenceHelper.setPreference(getPreferenceKey(contentAuthor), true);
                preferenceHelper.setPreference(getPreferenceKey(contentFavourites), false);
                preferenceHelper.setPreference(getPreferenceKey(contentSearch), false);
                break;

            case FAVOURITES:
                preferenceHelper.setPreference(getPreferenceKey(contentAll), false);
                preferenceHelper.setPreference(getPreferenceKey(contentAuthor), false);
                preferenceHelper.setPreference(getPreferenceKey(contentFavourites), true);
                preferenceHelper.setPreference(getPreferenceKey(contentSearch), false);
                break;

            case SEARCH:
                preferenceHelper.setPreference(getPreferenceKey(contentAll), false);
                preferenceHelper.setPreference(getPreferenceKey(contentAuthor), false);
                preferenceHelper.setPreference(getPreferenceKey(contentFavourites), false);
                preferenceHelper.setPreference(getPreferenceKey(contentSearch), true);
                break;

            default:
                Timber.e(contentSelection.toString());
                break;
        }
    }
}
