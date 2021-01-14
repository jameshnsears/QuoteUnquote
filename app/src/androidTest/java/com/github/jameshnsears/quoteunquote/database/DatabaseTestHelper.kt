package com.github.jameshnsears.quoteunquote.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.database.history.AbstractDatabaseHistory
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.After
import org.junit.Before
import java.util.ArrayList

open class DatabaseTestHelper {
    protected var databaseRepositoryDouble = DatabaseRepositoryDouble.getInstance()

    protected lateinit var quoteUnquoteModel: QuoteUnquoteModelDouble

    /*
        @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public CountingTaskExecutorRule countingTaskExecutorRule = new CountingTaskExecutorRule();

    protected static int widgetID = 1;

     */
    @Before
    fun setUp() {
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(AbstractDatabaseHistory.DATABASE_NAME)
        quoteUnquoteModel = QuoteUnquoteModelDouble()
    }

    @After
    fun tearDown() {
        quoteUnquoteModel.shutdown()
    }

    fun insertTestDataSet01() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
                QuotationEntity(
                        DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"
                ))
        quotationEntityList.add(
                QuotationEntity(
                        "d1", "a1", "q1"
                ))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertTestDataSet02() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
                QuotationEntity(
                        "d2", "a2", "q1"
                ))
        quotationEntityList.add(
                QuotationEntity(
                        "d3", "a2", "q3"
                ))
        quotationEntityList.add(
                QuotationEntity(
                        "d4", "a4", "q1"
                ))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertTestDataSet03() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
                QuotationEntity(
                        "d5", "a5", "q1"
                ))
        quotationEntityList.add(
                QuotationEntity(
                        "d6", "a2", "q6"
                ))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun setDefaultQuotation() {
        val defaultQuotation = databaseRepositoryDouble.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST)
        databaseRepositoryDouble.markAsPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, defaultQuotation.digest)
    }
}