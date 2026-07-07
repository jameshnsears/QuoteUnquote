package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ExclusionNextTest : QuoteUnquoteModelUtility() {
    private fun insertExclusionQuotationTestData() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(true),
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
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)
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

        assertThat(
            databaseRepositoryDouble
                .getAllExcludedDigests(
                    true,
                    quotationsPreferences.contentSelectionAllExclusion,
                ).containsAll(listOf("d6789012", "d6789013")),
            `is`(true),
        )

        assertThat(
            databaseRepositoryDouble
                .getNextDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quotationsPreferences.contentSelectionAllExclusion,
                    quotationsPreferences,
                ).containsAll(listOf("d6789014", "d6789015", "d6789016")),
            `is`(true),
        )

        assertThat(
            databaseRepositoryDouble
                .getPreviousDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quotationsPreferences.contentSelectionAllExclusion,
                ).containsAll(listOf(DatabaseRepository.getDefaultQuotationDigest(true))),
            `is`(true),
        )

        assertThat(
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest,
            equalTo("d6789014"),
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

        assertThat(
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest,
            equalTo("d6789014"),
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa3"

        // a1 = DatabaseRepository.getDefaultQuotationDigest()
        // a2 = "d6789012"
        // a2 = "d6789013"
        // a3 = "d6789014" = excluded
        // a3 = "d6789015" = excluded
        // a4 = "d6789016"

        assertThat(
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest,
            equalTo("d6789012"),
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

        assertThat(
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest,
            equalTo("d6789012"),
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaa3;aaa4"

        assertThat(
            quoteUnquoteModelDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, false).digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )
    }
}
