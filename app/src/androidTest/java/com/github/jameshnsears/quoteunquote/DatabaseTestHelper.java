package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;

import androidx.arch.core.executor.testing.CountingTaskExecutorRule;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;

public class DatabaseTestHelper {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public CountingTaskExecutorRule countingTaskExecutorRule = new CountingTaskExecutorRule();

    protected static int widgetID = 1;
    protected QuoteUnquoteModelFake quoteUnquoteModel;

    @Before
    public void setUp() {
        ApplicationProvider.getApplicationContext().deleteDatabase(AbstractHistoryDatabase.DATABASE_NAME);
        quoteUnquoteModel = new QuoteUnquoteModelFake();
    }

    @After
    public void tearDown() {
        quoteUnquoteModel.shutdown();
    }

    protected void insertTestDataSet01() {
        final List<QuotationEntity> quotationEntityList = new ArrayList<>();
        quotationEntityList.add(
                new QuotationEntity(
                        DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d1", "a1", "q1"
                ));
        quoteUnquoteModel.getDatabaseRepositoryFake().insertQuotations(quotationEntityList);
    }

    protected void insertTestDataSet02() {
        final List<QuotationEntity> quotationEntityList = new ArrayList<>();
        quotationEntityList.add(
                new QuotationEntity(
                        "d2", "a2", "q1"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d3", "a2", "q3"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d4", "a4", "q1"
                ));
        quoteUnquoteModel.getDatabaseRepositoryFake().insertQuotations(quotationEntityList);
    }

    protected void insertTestDataSet03() {
        final List<QuotationEntity> quotationEntityList = new ArrayList<>();
        quotationEntityList.add(
                new QuotationEntity(
                        "d5", "a5", "q1"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d6", "a2", "q6"
                ));
        quoteUnquoteModel.getDatabaseRepositoryFake().insertQuotations(quotationEntityList);
    }

    protected void setDefaultQuotation() {
        quoteUnquoteModel.setDefaultQuotation(widgetID);

        assertEquals(DatabaseRepository.DEFAULT_QUOTATION_DIGEST,
                quoteUnquoteModel.getNext(
                        widgetID, ContentType.ALL).digest);

        assertEquals(
                1,
                quoteUnquoteModel.countPrevious(widgetID, ContentType.ALL));
    }
}
