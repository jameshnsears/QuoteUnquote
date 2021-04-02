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
    private static final String CONTENT_SEARCH_TEXT = "CONTENT_SEARCH_TEXT";

    public ContentPreferences(@NonNull final Context applicationContext) {
        super(0, applicationContext);
    }

    public ContentPreferences(final int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public String getContentSelectionAuthor() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(CONTENT_AUTHOR_NAME));
    }

    public void setContentSelectionAuthor(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(CONTENT_AUTHOR_NAME), value);
    }

    @NonNull
    public String getContentFavouritesLocalCode() {
        return preferenceHelper.getPreferenceString(getFavouritesLocalCode());
    }

    public void setContentFavouritesLocalCode(@NonNull final String value) {
        preferenceHelper.setPreference(getFavouritesLocalCode(), value);
    }

    @NonNull
    public String getContentSelectionSearch() {
        return preferenceHelper.getPreferenceString(getPreferenceKey(CONTENT_SEARCH_TEXT));
    }

    public void setContentSelectionSearch(@NonNull final String value) {
        preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH_TEXT), value);
    }

    @NonNull
    public ContentSelection getContentSelection() {
        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(CONTENT_AUTHOR), false)) {
            return ContentSelection.AUTHOR;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(CONTENT_FAVOURITES), false)) {
            return ContentSelection.FAVOURITES;
        }

        if (preferenceHelper.getPreferenceBoolean(getPreferenceKey(CONTENT_SEARCH), false)) {
            return ContentSelection.SEARCH;
        }

        return ContentSelection.ALL;
    }

    public void setContentSelection(@NonNull final ContentSelection contentSelection) {
        switch (contentSelection) {
            case ALL:
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_ALL), true);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_AUTHOR), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_FAVOURITES), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH), false);
                break;

            case AUTHOR:
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_ALL), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_AUTHOR), true);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_FAVOURITES), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH), false);
                break;

            case FAVOURITES:
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_ALL), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_AUTHOR), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_FAVOURITES), true);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH), false);
                break;

            case SEARCH:
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_ALL), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_AUTHOR), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_FAVOURITES), false);
                preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH), true);
                break;

            default:
                Timber.e(contentSelection.toString());
                break;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getContentSelection().toString();
    }

    public void performMigration() {
        final Map<String, ?> sharedPreferenceEntries
                = applicationContext.getSharedPreferences("QuoteUnquote-Preferences", Context.MODE_PRIVATE).getAll();

        for (final Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            widgetId = Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf(":")));

            if (entry.getKey().contains("FragmentContent:radioButtonAll")) {
                boolean radioButtonAll = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonAll=%b", widgetId, radioButtonAll);
                if (radioButtonAll) {
                    setContentSelection(ContentSelection.ALL);
                }
            }

            if (entry.getKey().contains("FragmentContent:radioButtonAuthor")) {
                boolean radioButtonAuthor = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonAuthor=%b", widgetId, radioButtonAuthor);
                if (radioButtonAuthor) {
                    setContentSelection(ContentSelection.AUTHOR);
                }
            }
            if (entry.getKey().contains("FragmentContent:spinnerAuthors")) {
                String spinnerAuthors = (String) entry.getValue();
                Timber.d("%d: spinnerAuthors=%s", widgetId, spinnerAuthors);
                setContentSelectionAuthor(spinnerAuthors);
            }

            if (entry.getKey().contains("FragmentContent:radioButtonFavourites")) {
                boolean radioButtonFavourites = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonFavourites=%b", widgetId, radioButtonFavourites);
                if (radioButtonFavourites) {
                    setContentSelection(ContentSelection.FAVOURITES);
                }
            }

            if (entry.getKey().equals("0:FragmentContent:textViewFavouritesCode")) {
                String textViewFavouritesCode = (String) entry.getValue();
                Timber.d("%d: textViewFavouritesCode=%s", widgetId, textViewFavouritesCode);
                setContentFavouritesLocalCode(textViewFavouritesCode);
            }

            if (entry.getKey().contains("FragmentContent:radioButtonKeywords")) {
                boolean radioButtonKeywords = (Boolean) entry.getValue();
                Timber.d("%d: radioButtonKeywords=%b", widgetId, radioButtonKeywords);
                if (radioButtonKeywords) {
                    setContentSelection(ContentSelection.SEARCH);
                }
            }
            if (entry.getKey().contains("FragmentContent:editTextKeywords")) {
                String editTextKeywords = (String) entry.getValue();
                Timber.d("%d: editTextKeywords=%s", widgetId, editTextKeywords);
                setContentSelectionSearch(editTextKeywords);
            }
        }
    }
}
