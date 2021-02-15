package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Before
import java.util.ArrayList

abstract class DatabaseTestHelper {
    var databaseRepositoryDouble = DatabaseRepositoryDouble.getInstance()

    @Before
    fun before() {
        databaseRepositoryDouble.empty()
    }

    fun insertDataset01() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.DEFAULT_QUOTATION_DIGEST, "a0", "q0"
            )
        )
        quotationEntityList.add(QuotationEntity("d1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertDataset02() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2", "a2", "q1"))
        quotationEntityList.add(QuotationEntity("d3", "a2", "q3"))
        quotationEntityList.add(QuotationEntity("d4", "a4", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertDataset03() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d5", "a5", "q1"))
        quotationEntityList.add(QuotationEntity("d6", "a2", "q6"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun setDefaultQuotation() {
        val defaultQuotation = databaseRepositoryDouble.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST)
        databaseRepositoryDouble.markAsPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, defaultQuotation.digest)
    }
}
