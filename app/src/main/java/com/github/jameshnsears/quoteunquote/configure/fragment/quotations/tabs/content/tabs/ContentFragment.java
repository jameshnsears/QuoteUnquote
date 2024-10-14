package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs;

import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

@Keep
public class ContentFragment extends FragmentCommon {
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    public QuotationsPreferences quotationsPreferences;

    public ContentFragment(int widgetId) {
        super(widgetId);
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());
        quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());
    }

    protected void updateQuotationsPreferences() {
        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getContext());
        quotationsPreferences.setContentSelection(ContentSelection.ALL);
    }

    protected void importWasSuccessful() {
        quotationsPreferences.setContentSelectionSearchCount(0);
        quotationsPreferences.setContentSelectionSearch("");

        DatabaseRepository.useInternalDatabase = false;

        updateQuotationsPreferences();
    }

    protected void useInternalDatabase() {
        this.quotationsPreferences.setDatabaseInternal(true);
        this.quotationsPreferences.setDatabaseExternalCsv(false);
        this.quotationsPreferences.setDatabaseExternalWeb(false);
        DatabaseRepository.useInternalDatabase = true;

        updateQuotationsPreferences();
    }
}
