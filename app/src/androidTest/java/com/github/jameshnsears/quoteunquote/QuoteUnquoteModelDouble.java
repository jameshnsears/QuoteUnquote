package com.github.jameshnsears.quoteunquote;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.List;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class QuoteUnquoteModelDouble extends QuoteUnquoteModel {
    public QuoteUnquoteModelDouble() {
        this.context = getApplicationContext();
        this.databaseRepository = DatabaseRepositoryDouble.getInstance();
    }

    public int countReported() {
        return DatabaseRepositoryDouble.getInstance().countReported();
    }

    @NonNull
    public List<String> getFavourites() {
        return this.databaseRepository.getFavourites();
    }

    public int countPrevious(int widgetId) {
        return this.databaseRepository.countPrevious(widgetId, ContentSelection.ALL)
                + this.databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR)
                + this.databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH);
    }

    public List<QuotationEntity> getAllQuotations() {
        return databaseRepository.getAllQuotations();
    }
}
