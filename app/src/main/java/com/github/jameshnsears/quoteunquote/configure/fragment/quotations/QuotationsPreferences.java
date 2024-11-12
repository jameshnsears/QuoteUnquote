package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import timber.log.Timber;

public class QuotationsPreferences extends PreferencesFacade {
    public static final String SCREEN = "SCREEN";

    public static final String CONTENT_ALL = "CONTENT_ALL";
    public static final String CONTENT_ALL_EXCLUSION = "CONTENT_ALL_EXCLUSION";
    public static final String CONTENT_ADD_TO_PREVIOUS_ALL = "CONTENT_ADD_TO_PREVIOUS_ALL";
    public static final String CONTENT_AUTHOR = "CONTENT_AUTHOR";
    public static final String CONTENT_AUTHOR_NAME = "CONTENT_AUTHOR_NAME";
    public static final String CONTENT_AUTHOR_NAME_COUNT = "CONTENT_AUTHOR_NAME_COUNT";
    public static final String CONTENT_FAVOURITES = "CONTENT_FAVOURITES";
    public static final String CONTENT_SEARCH = "CONTENT_SEARCH";
    public static final String CONTENT_SEARCH_FAVOURITES_ONLY = "CONTENT_SEARCH_FAVOURITES_ONLY";
    public static final String CONTENT_SEARCH_REGEX = "CONTENT_SEARCH_REGEX";
    public static final String CONTENT_SEARCH_COUNT = "CONTENT_SEARCH_COUNT";
    public static final String CONTENT_SEARCH_TEXT = "CONTENT_SEARCH_TEXT";
    public static final String CONTENT_SEARCH_FORCE_ENABLE_BUTTONS = "CONTENT_SEARCH_FORCE_ENABLE_BUTTONS";

    public static final String DATABASE_INTERNAL = "DATABASE_INTERNAL";
    public static final String DATABASE_EXTERNAL = "DATABASE_EXTERNAL";
    public static final String DATABASE_EXTERNAL_WEB = "DATABASE_EXTERNAL_WEB";
    public static final String DATABASE_WEB_URL = "DATABASE_WEB_URL";
    public static final String DATABASE_WEB_XPATH_QUOTATION = "DATABASE_WEB_XPATH_QUOTATION";
    public static final String DATABASE_WEB_XPATH_SOURCE = "DATABASE_WEB_XPATH_SOURCE";
    public static final String DATABASE_WEB_KEEP_LATEST_ONLY = "DATABASE_WEB_KEEP_LATEST_ONLY";

    public static final String DATABASE_EXTERNAL_CONTENT = "DATABASE_EXTERNAL_CONTENT";

    @NonNull
    public String getScreen() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(SCREEN));
    }

    public void setScreen(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(SCREEN), value);
    }

    public QuotationsPreferences(@NonNull Context applicationContext) {
        super(0, applicationContext);
    }

    public QuotationsPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public boolean getDatabaseWebKeepLatestOnly() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_KEEP_LATEST_ONLY), true);
    }

    public void setDatabaseWebKeepLatestOnly(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_KEEP_LATEST_ONLY), value);
    }

    @NonNull
    public String getDatabaseExternalContent() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(DATABASE_EXTERNAL_CONTENT));
    }

    public void setDatabaseExternalContent(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(DATABASE_EXTERNAL_CONTENT), value);
    }

    @NonNull
    public boolean getDatabaseInternal() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_INTERNAL), true);
    }

    public void setDatabaseInternal(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_INTERNAL), value);
    }

    @NonNull
    public boolean getDatabaseExternalCsv() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL), false);
    }

    public void setDatabaseExternalCsv(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL), value);
    }

    @NonNull
    public String getDatabaseWebXpathSource() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_XPATH_SOURCE));
    }

    public void setDatabaseWebXpathSource(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_XPATH_SOURCE), value);
    }

    @NonNull
    public String getDatabaseWebXpathQuotation() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_XPATH_QUOTATION));
    }

    public void setDatabaseWebXpathQuotation(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_XPATH_QUOTATION), value);
    }

    @NonNull
    public String getDatabaseWebUrl() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_URL));
    }

    public void setDatabaseWebUrl(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_WEB_URL), value);
    }

    @NonNull
    public boolean getDatabaseExternalWeb() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL_WEB), false);
    }

    public void setDatabaseExternalWeb(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.DATABASE_EXTERNAL_WEB), value);
    }

    @NonNull
    public boolean getContentSelectionSearchFavouritesOnly() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FAVOURITES_ONLY), false);
    }

    public void setContentSelectionSearchFavouritesOnly(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FAVOURITES_ONLY), value);
    }

    @NonNull
    public boolean getContentSelectionSearchForceEnableButtons() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FORCE_ENABLE_BUTTONS), true);
    }

    public void setContentSelectionSearchForceEnableButtons(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_FORCE_ENABLE_BUTTONS), value);
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
    public boolean getContentSelectionSearchRegEx() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_REGEX), false);
    }

    public void setContentSelectionSearchRegEx(@NonNull boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(QuotationsPreferences.CONTENT_SEARCH_REGEX), value);
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
