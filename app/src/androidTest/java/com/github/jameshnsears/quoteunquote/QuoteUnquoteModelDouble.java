package com.github.jameshnsears.quoteunquote;

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

    public int countFavourites() {
        return databaseRepository.countFavourites().blockingGet().intValue();
    }

    public int countReported() {
        return DatabaseRepositoryDouble.getInstance().countReported();
    }

    public List<String> getPrevious(final int widgetId, final ContentSelection contentSelection) {
        return databaseRepository.getPrevious(widgetId, contentSelection);
    }

    public List<String> getFavourites() {
        return databaseRepository.getFavourites();
    }
}
