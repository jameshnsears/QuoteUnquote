package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class QuoteUnquoteModelDouble extends QuoteUnquoteModel {
    public QuoteUnquoteModelDouble() {
        super();
        this.context = getApplicationContext();
        databaseRepository = DatabaseRepositoryDouble.getInstance();
    }

    public void shutdown() {
        super.shutdown();
    }

    public Single<Integer> countFavourites() {
        return databaseRepository.countFavourites();
    }

    public Integer countReported() {
        return ((DatabaseRepositoryDouble) databaseRepository).countReported();
    }

    public List<String> getPrevious(final int widgetId, final ContentSelection contentSelection) {
        return databaseRepository.getPrevious(widgetId, contentSelection);
    }

    public List<String> getFavourites() {
        return databaseRepository.getFavourites();
    }

    @Override
    public String getPreferencesTextSearch(final int widgetId) {
        return "q1";
    }

    @Override
    public ContentSelection getSelectedContentType(final int widgetId) {
        return ContentSelection.ALL;
    }

    public void insertTestDataSet01() {
        List<QuotationEntity> quotationEntityList = new ArrayList();

        quotationEntityList.add(new QuotationEntity(DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"));
        quotationEntityList.add(new QuotationEntity("d1", "a1", "q1"));

        ((DatabaseRepositoryDouble) databaseRepository).insertQuotations(quotationEntityList);
    }
}
