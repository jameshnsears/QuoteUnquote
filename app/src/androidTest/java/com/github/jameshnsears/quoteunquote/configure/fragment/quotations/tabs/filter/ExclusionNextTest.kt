package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExclusionNextTest : QuoteUnquoteModelUtility() {
    private fun insertExclusionQuotationTestData() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "aaaa1",
                "q1"
            )
        )
        quotationEntityList.add(QuotationEntity("d6789012", "w1", "aaaa2", "q2"))
        quotationEntityList.add(QuotationEntity("d6789013", "w1", "aaaa2", "q3"))
        quotationEntityList.add(QuotationEntity("d6789014", "w1", "aaaa3", "q4"))
        quotationEntityList.add(QuotationEntity("d6789015", "w1", "aaaa3", "q5"))
        quotationEntityList.add(QuotationEntity("d6789016", "w1", "aaaa4", "q6"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    @Test
    fun exclusionSingleCriteria() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa2"

        quoteUnquoteModelDouble.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)

        // a1 = DatabaseRepository.getDefaultQuotationDigest()
        // a2 = "d6789012" = excluded
        // a2 = "d6789013" = excluded
        // a3 = "d6789014"
        // a3 = "d6789015"
        // a4 = "d6789016"

        assertTrue(
            databaseRepositoryDouble.getAllExcludedDigests(
                quotationsPreferences.contentSelectionAllExclusion
            )
                .containsAll(listOf("d6789012", "d6789013"))
        )

        assertTrue(
            databaseRepositoryDouble
                .getNextDigests(
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quotationsPreferences.contentSelectionAllExclusion
                )
                .containsAll(listOf("d6789014", "d6789015", "d6789016"))
        )

        assertTrue(
            databaseRepositoryDouble
                .getPreviousDigests(
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quotationsPreferences.contentSelectionAllExclusion
                )
                .containsAll(listOf(DatabaseRepository.getDefaultQuotationDigest()))
        )

        assertEquals(
            "d6789014",
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest
        )
    }

    @Test
    fun exclusionSwitchMultipleCriteria() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa2"

        quoteUnquoteModelDouble.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)

        // a1 = DatabaseRepository.getDefaultQuotationDigest()
        // a2 = "d6789012" = excluded
        // a2 = "d6789013" = excluded
        // a3 = "d6789014"
        // a3 = "d6789015"
        // a4 = "d6789016"

        assertEquals(
            "d6789014",
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa3"

        // a1 = DatabaseRepository.getDefaultQuotationDigest()
        // a2 = "d6789012"
        // a2 = "d6789013"
        // a3 = "d6789014" = excluded
        // a3 = "d6789015" = excluded
        // a4 = "d6789016"

        assertEquals(
            "d6789012",
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest
        )
    }

    @Test
    fun exclusionOfCurrentQuotation() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = ""

        quoteUnquoteModelDouble.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)

        // a1 = DatabaseRepository.getDefaultQuotationDigest()
        // a2 = "d6789012"
        // a2 = "d6789013"
        // a3 = "d6789014"
        // a3 = "d6789015"
        // a4 = "d6789016"

        assertEquals(
            "d6789012",
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaa3;aaa4"

        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest
        )
    }

    @Test
    fun confirmCurrentAfterChangingExclusion() {
        /*
    exclusion = a3
    next, sequential * 7 =  q2.2/a2

    exclusion = a
    current / next (any) = q1.1/a1

     exclusion = a3
     current / next (any) = q2.2/a2

    exclusion = none
    current = q2.2/a2

      next, sequential * 3 =  q3.3/a3
     */
    }
}
