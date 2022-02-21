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
    private static final String CONTENT_ADD_TO_PREVIOUS_ALL = "CONTENT_ADD_TO_PREVIOUS_ALL";

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
    public boolean getContentAddToPreviousAll() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(CONTENT_ADD_TO_PREVIOUS_ALL), false);
    }

    public void setContentAddToPreviousAll(@NonNull final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(CONTENT_ADD_TO_PREVIOUS_ALL), value);
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

    public int getContentSelectionSearchCount() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(CONTENT_SEARCH_COUNT));
    }

    public void setContentSelectionSearchCount(int value) {
        preferenceHelper.setPreference(getPreferenceKey(CONTENT_SEARCH_COUNT), value);
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

            this.migrationRadioButtonAll(entry, "FragmentContent:radioButtonAll", "%d: radioButtonAll=%b", ContentSelection.ALL);

            this.migrationRadioButtonAll(entry, "FragmentContent:radioButtonAuthor", "%d: radioButtonAuthor=%b", ContentSelection.AUTHOR);

            this.migrationSpinnerAuthors(entry);

            this.migrationRadioButtonAll(entry, "FragmentContent:radioButtonFavourites", "%d: radioButtonFavourites=%b", ContentSelection.FAVOURITES);

            this.migrationTextViewFavouritesCode(entry);

            this.migrationRadioButtonAll(entry, "FragmentContent:radioButtonKeywords", "%d: radioButtonKeywords=%b", ContentSelection.SEARCH);

            this.migrationEditTextKeywords(entry);
        }
    }

    private void migrationEditTextKeywords(final Map.Entry<String, ?> entry) {
        if (entry.getKey().contains("FragmentContent:editTextKeywords")) {
            String editTextKeywords = (String) entry.getValue();
            Timber.d("%d: editTextKeywords=%s", widgetId, editTextKeywords);
            setContentSelectionSearch(editTextKeywords);
        }
    }

    private void migrationTextViewFavouritesCode(final Map.Entry<String, ?> entry) {
        if (entry.getKey().equals("0:FragmentContent:textViewFavouritesCode")) {
            String textViewFavouritesCode = (String) entry.getValue();
            Timber.d("%d: textViewFavouritesCode=%s", widgetId, textViewFavouritesCode);
            setContentFavouritesLocalCode(textViewFavouritesCode);
        }
    }

    private void migrationSpinnerAuthors(final Map.Entry<String, ?> entry) {
        if (entry.getKey().contains("FragmentContent:spinnerAuthors")) {
            String spinnerAuthors = (String) entry.getValue();
            Timber.d("%d: spinnerAuthors=%s", widgetId, spinnerAuthors);
            setContentSelectionAuthor(spinnerAuthors);
        }
    }

    private void migrationRadioButtonAll(final Map.Entry<String, ?> entry, final String s, final String s2, final ContentSelection all) {
        if (entry.getKey().contains(s)) {
            boolean radioButtonAll = (Boolean) entry.getValue();
            Timber.d(s2, widgetId, radioButtonAll);
            if (radioButtonAll) {
                setContentSelection(all);
            }
        }
    }
}
