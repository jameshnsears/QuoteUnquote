package com.github.jameshnsears.quoteunquote.database;

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTestHelper {
    protected DatabaseRepositoryDouble databaseRepositoryDouble = DatabaseRepositoryDouble.getInstance();

    /*
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
     */

    public void insertTestDataSet01() {
        final List<QuotationEntity> quotationEntityList = new ArrayList<>();

        quotationEntityList.add(
                new QuotationEntity(
                        DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d1", "a1", "q1"
                ));

        databaseRepositoryDouble.insertQuotations(quotationEntityList);
    }

    public void insertTestDataSet02() {
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

        databaseRepositoryDouble.insertQuotations(quotationEntityList);
    }

    public void insertTestDataSet03() {
        final List<QuotationEntity> quotationEntityList = new ArrayList<>();

        quotationEntityList.add(
                new QuotationEntity(
                        "d5", "a5", "q1"
                ));
        quotationEntityList.add(
                new QuotationEntity(
                        "d6", "a2", "q6"
                ));

        databaseRepositoryDouble.insertQuotations(quotationEntityList);
    }

    public void setDefaultQuotation() {
        QuotationEntity defaultQuotation = databaseRepositoryDouble.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST);
        databaseRepositoryDouble.markAsPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, defaultQuotation.digest);
    }
}
