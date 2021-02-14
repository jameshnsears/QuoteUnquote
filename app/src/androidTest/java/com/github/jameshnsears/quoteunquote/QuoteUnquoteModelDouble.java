package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.List;

import io.reactivex.Single;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class QuoteUnquoteModelDouble extends QuoteUnquoteModel {
    public QuoteUnquoteModelDouble() {
        super();
        context = getApplicationContext();
        databaseRepository = DatabaseRepositoryDouble.getInstance();
    }

    public Single<Integer> countFavourites() {
        return databaseRepository.countFavourites();
    }

    public Integer countReported() {
        return DatabaseRepositoryDouble.getInstance().countReported();
    }

    public List<String> getPrevious(final int widgetId, final ContentSelection contentSelection) {
        return databaseRepository.getPrevious(widgetId, contentSelection);
    }

    public List<String> getFavourites() {
        return databaseRepository.getFavourites();
    }

    @Override
    public ContentSelection selectedContentType(final int widgetId) {
        return ContentSelection.ALL;
    }
}
