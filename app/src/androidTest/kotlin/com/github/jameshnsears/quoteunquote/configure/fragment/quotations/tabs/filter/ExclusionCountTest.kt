package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class ExclusionCountTest : QuoteUnquoteModelUtility() {
    private fun insertExclusionQuotationTestData() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "aaaa1",
                "q1",
            ),
        )
        quotationEntityList.add(QuotationEntity("d6789012", "w1", "aaaa2", "q2"))
        quotationEntityList.add(QuotationEntity("d6789013", "w1", "aaaa2", "q3"))
        quotationEntityList.add(QuotationEntity("d6789014", "w1", "aaaa3", "q4"))
        quotationEntityList.add(QuotationEntity("d6789015", "w1", "aaaa3", "q5"))
        quotationEntityList.add(QuotationEntity("d6789016", "w1", "aaaa4", "q6"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    @Test
    fun countAllNoSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        assertEquals("", quotationsPreferences.contentSelectionAllExclusion)

        assertEquals(
            6,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )
    }

    @Test
    fun countAllSingleSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa2"

        assertEquals(
            4,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )
    }

    @Test
    fun countAllSingleExclusionKeepDefaultQuotation() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa"

        assertEquals(
            1,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )
    }

    @Test
    fun countAllMultipleSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaaa3"
        assertEquals(
            2,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaaa"
        assertEquals(
            1,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )

        quotationsPreferences.contentSelectionAllExclusion = ";;;;;;*;()"
        assertEquals(
            6,
            quoteUnquoteModelDouble.countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
        )
    }
}
