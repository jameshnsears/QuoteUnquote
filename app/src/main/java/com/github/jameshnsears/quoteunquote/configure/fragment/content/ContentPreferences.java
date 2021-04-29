package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import java.util.Map;

import timber.log.Timber;

public class ContentPreferences extends PreferencesFacade {
    private static final String CONTENT_ALL = "CONTENT_ALL";
    private static final String CONTENT_AUTHOR = "CONTENT_AUTHOR";
    private static final String CONTENT_AUTHOR_NAME = "CONTENT_AUTHOR_NAME";
    private static final String CONTENT_FAVOURITES = "CONTENT_FAVOURITES";
    private static final String CONTENT_SEARCH = "CONTENT_SEARCH";
    private static final String CONTENT_SEARCH_COUNT = "CONTENT_SEARCH_COUNT";
    private static final String CONTENT_SEARCH_TEXT = "CONTENT_SEARCH_TEXT";

    public ContentPreferences(@NonNull Context applicationContext) {
        super(0, applicationContext);
    }

    public ContentPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public String getContentSelectionAuthor() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR_NAME));
    }

    public void setContentSelectionAuthor(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR_NAME), value);
    }

    @NonNull
    public String getContentFavouritesLocalCode() {
        return this.preferenceHelper.getPreferenceString(this.getFavouritesLocalCode());
    }

    public void setContentFavouritesLocalCode(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getFavouritesLocalCode(), value);
    }

    @NonNull
    public String getContentSelectionSearch() {
        return this.preferenceHelper.getPreferenceString(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH_TEXT));
    }

    public void setContentSelectionSearch(@NonNull String value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH_TEXT), value);
    }

    public int getContentSelectionSearchCount() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH_COUNT));
    }

    public void setContentSelectionSearchCount(final int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH_COUNT), value);
    }

    @NonNull
    public ContentSelection getContentSelection() {
        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR), false)) {
            return ContentSelection.AUTHOR;
        }

        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(ContentPreferences.CONTENT_FAVOURITES), false)) {
            return ContentSelection.FAVOURITES;
        }

        if (this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH), false)) {
            return ContentSelection.SEARCH;
        }

        return ContentSelection.ALL;
    }

    public void setContentSelection(@NonNull ContentSelection contentSelection) {
        switch (contentSelection) {
            case ALL:
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_ALL), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH), false);
                break;

            case AUTHOR:
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH), false);
                break;

            case FAVOURITES:
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_FAVOURITES), true);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH), false);
                break;

            case SEARCH:
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_ALL), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_AUTHOR), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_FAVOURITES), false);
                this.preferenceHelper.setPreference(this.getPreferenceKey(ContentPreferences.CONTENT_SEARCH), true);
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

    public void performMigration() {
        Map<String, ?> sharedPreferenceEntries
                = this.applicationContext.getSharedPreferences("QuoteUnquote-Preferences", Context.MODE_PRIVATE).getAll();

        for (Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            this.widgetId = Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf(":")));

            if (entry.getKey().contains("FragmentContent:radioButtonAll")) {
                final boolean radioButtonAll = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonAll=%b", this.widgetId, radioButtonAll);
                if (radioButtonAll) {
                    this.setContentSelection(ContentSelection.ALL);
                }
            }

            if (entry.getKey().contains("FragmentContent:radioButtonAuthor")) {
                final boolean radioButtonAuthor = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonAuthor=%b", this.widgetId, radioButtonAuthor);
                if (radioButtonAuthor) {
                    this.setContentSelection(ContentSelection.AUTHOR);
                }
            }
            if (entry.getKey().contains("FragmentContent:spinnerAuthors")) {
                final String spinnerAuthors = (String) entry.getValue();
                Timber.d("%d: spinnerAuthors=%s", this.widgetId, spinnerAuthors);
                this.setContentSelectionAuthor(spinnerAuthors);
            }

            if (entry.getKey().contains("FragmentContent:radioButtonFavourites")) {
                final boolean radioButtonFavourites = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonFavourites=%b", this.widgetId, radioButtonFavourites);
                if (radioButtonFavourites) {
                    this.setContentSelection(ContentSelection.FAVOURITES);
                }
            }

            if (entry.getKey().equals("0:FragmentContent:textViewFavouritesCode")) {
                final String textViewFavouritesCode = (String) entry.getValue();
                Timber.d("%d: textViewFavouritesCode=%s", this.widgetId, textViewFavouritesCode);
                this.setContentFavouritesLocalCode(textViewFavouritesCode);
            }

            if (entry.getKey().contains("FragmentContent:radioButtonKeywords")) {
                final boolean radioButtonKeywords = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonKeywords=%b", this.widgetId, radioButtonKeywords);
                if (radioButtonKeywords) {
                    this.setContentSelection(ContentSelection.SEARCH);
                }
            }
            if (entry.getKey().contains("FragmentContent:editTextKeywords")) {
                final String editTextKeywords = (String) entry.getValue();
                Timber.d("%d: editTextKeywords=%s", this.widgetId, editTextKeywords);
                this.setContentSelectionSearch(editTextKeywords);
            }
        }
    }
}
