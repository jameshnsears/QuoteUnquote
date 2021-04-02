package com.github.jameshnsears.quoteunquote;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.List;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class QuoteUnquoteModelDouble extends QuoteUnquoteModel {
    public QuoteUnquoteModelDouble() {
        super();
        context = getApplicationContext();
        databaseRepository = DatabaseRepositoryDouble.getInstance();
    }

    public int countReported() {
        return DatabaseRepositoryDouble.getInstance().countReported();
    }

    @NonNull
    public List<String> getFavourites() {
        return databaseRepository.getFavourites();
    }

    public int countPrevious(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.ALL)
                + databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR)
                + databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH);
    }
}
