package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ExclusionCountTest : QuoteUnquoteModelUtility() {
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
    fun countAllNoSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        assertThat(quotationsPreferences.contentSelectionAllExclusion, equalTo(""))

        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(6),
        )
    }

    @Test
    fun countAllSingleSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa2"

        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(4),
        )
    }

    @Test
    fun countAllSingleExclusionKeepDefaultQuotation() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        quotationsPreferences.contentSelectionAllExclusion = "aaaa"

        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(1),
        )
    }

    @Test
    fun countAllMultipleSourceExclusion() {
        insertExclusionQuotationTestData()

        val quotationsPreferences = QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaaa3"
        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(2),
        )

        quotationsPreferences.contentSelectionAllExclusion = "aaaa2;aaaa"
        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(1),
        )

        quotationsPreferences.contentSelectionAllExclusion = ";;;;;;*;()"
        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(6),
        )
    }
}
