package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.junit.Before
import java.util.ArrayList

abstract class DatabaseTestHelper {
    var databaseRepositoryDouble = DatabaseRepositoryDouble.getInstance()

    @Before
    fun before() {
        databaseRepositoryDouble.empty()
    }

    fun insertQuotationsTestData01() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"
            )
        )
        quotationEntityList.add(QuotationEntity("d1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertQuotationsTestData02() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2", "a2", "q1"))
        quotationEntityList.add(QuotationEntity("d3", "a2", "q3"))
        quotationEntityList.add(QuotationEntity("d4", "a4", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertQuotationsTestData03() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d5", "a5", "q1"))
        quotationEntityList.add(QuotationEntity("d6", "a2", "q6"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun setDefaultQuotationAsPreviousAll(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.ALL,
            getDefaultQuotation().digest
        )
    }

    fun setDefaultQuotationAsPreviousAuthor(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.AUTHOR,
            getDefaultQuotation().digest
        )
    }

    fun setDefaultQuotationAsPreviousSearch(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.SEARCH,
            getDefaultQuotation().digest
        )
    }

    fun markDefaultQuotationAsFavourite() {
        databaseRepositoryDouble.markAsFavourite(getDefaultQuotation().digest)
    }

    fun markDefaultQuotationAsReported() {
        databaseRepositoryDouble.markAsReported(getDefaultQuotation().digest)
    }

    private fun getDefaultQuotation(): QuotationEntity {
        return databaseRepositoryDouble.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST)
    }
}
