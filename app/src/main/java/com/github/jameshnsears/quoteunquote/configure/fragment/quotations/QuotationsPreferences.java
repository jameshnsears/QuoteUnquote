package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import timber.log.Timber;

public class QuotationsPreferences extends PreferencesFacade {
    public static final String CONTENT_ALL = "CONTENT_ALL";
    public static final String CONTENT_ALL_EXCLUSION = "CONTENT_ALL_EXCLUSION";
    public static final String CONTENT_ADD_TO_PREVIOUS_ALL = "CONTENT_ADD_TO_PREVIOUS_ALL";
    public static final String CONTENT_AUTHOR = "CONTENT_AUTHOR";
    public static final String CONTENT_AUTHOR_NAME = "CONTENT_AUTHOR_NAME";
    public static final String CONTENT_AUTHOR_NAME_COUNT = "CONTENT_AUTHOR_NAME_COUNT";
    public static final String CONTENT_FAVOURITES = "CONTENT_FAVOURITES";
    public static final String CONTENT_SEARCH = "CONTENT_SEARCH";
    public static final String CONTENT_SEARCH_FAVOURITES_ONLY = "CONTENT_SEARCH_FAVOURITES_ONLY";
    public static final String CONTENT_SEARCH_COUNT = "CONTENT_SEARCH_COUNT";
    public static final String CONTENT_SEARCH_TEXT = "CONTENT_SEARCH_TEXT";

    public static final String DATABASE_INTERNAL = "DATABASE_INTERNAL";
    public static final String DATABASE_EXTERNAL = "DATABASE_EXTERNAL";
    public static final String DATABASE_EXTERNAL_FILENAME = "DATABASE_EXTERNAL_PATH";

    @NonNull
    public boolean getDatabaseInternal() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_INTERNAL), true);
    }

    public void setDatabaseInternal(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_INTERNAL), value);
    }

    @NonNull
    public boolean getDatabaseExternal() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL), false);
    }

    public void setDatabaseExternal(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL), value);
    }

    public QuotationsPreferences(@NonNull Context applicationContext) {
        super(0, applicationContext);
    }

    public QuotationsPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public boolean getContentSelectionSearchFavouritesOnly() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FAVOURITES_ONLY), false);
    }

    public void setContentSelectionSearchFavouritesOnly(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FAVOURITES_ONLY), value);
    }

    @NonNull
    public Integer getContentSelectionAuthorCount() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR_NAME_COUNT));
    }

    public void setContentSelectionAuthorCount(@NonNull Integer value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR_NAME_COUNT), value);
    }

    @NonNull
    public String getContentSelectionAuthor() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR_NAME));
    }

    public void setContentSelectionAuthor(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR_NAME), value);
    }

    @NonNull
    public boolean getContentAddToPreviousAll() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_ADD_TO_PREVIOUS_ALL), true);
    }

    public void setContentAddToPreviousAll(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ADD_TO_PREVIOUS_ALL), value);
    }

    @NonNull
    public String getContentLocalCode() {
        return this.preferenceHelper.getPreferenceString(this.getLocalCode());
    }

    public void setContentLocalCode(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getLocalCode(), value);
    }

    @NonNull
    public String getContentSelectionSearch() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_TEXT));
    }

    public void setContentSelectionSearch(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_TEXT), value);
    }

    public String getContentSelectionAllExclusion() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL_EXCLUSION));
    }

    public void setContentSelectionAllExclusion(final String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL_EXCLUSION), value);
    }

    public int getContentSelectionSearchCount() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_COUNT));
    }

    public void setContentSelectionSearchCount(final int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_COUNT), value);
    }

    @NonNull
    public ContentSelection getContentSelection() {
        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR), false)) {
            return ContentSelection.AUTHOR;
        }

        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_FAVOURITES), false)) {
            return ContentSelection.FAVOURITES;
        }

        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH), false)) {
            return ContentSelection.SEARCH;
        }

        return ContentSelection.ALL;
    }

    public void setContentSelection(@NonNull ContentSelection contentSelection) {
        switch (contentSelection) {
            case ALL:
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH), false);
                break;

            case AUTHOR:
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH), false);
                break;

            case FAVOURITES:
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_FAVOURITES), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH), false);
                break;

            case SEARCH:
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH), true);
                break;

            default:
                Timber.e(contentSelection.toString());
                break;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return this.getContentSelection().toString();
    }
}
