package com.github.jameshnsears.quoteunquote.db

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SearchCountTest : QuoteUnquoteModelUtility() {
    @Test
    fun getSearchQuotations() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "aaa", "zzz1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "AAA", "ZZZ2"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "bbb", "bbbaaa"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertThat(
            quoteUnquoteModelDouble
                .getSearchQuotations(
                    "aaa",
                    false,
                ).size,
            equalTo(3),
        )

        assertThat(
            quoteUnquoteModelDouble
                .getSearchQuotations(
                    "bBb",
                    false,
                ).size,
            equalTo(1),
        )
    }

    @Test
    fun countSearchText() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "aaa", "zzz1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "AAA", "ZZZ2"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "bbb", "bbbaAA"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "aaa",
                false,
            ),
            equalTo(
                quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                    "aaa",
                    false,
                ),
            ),
        )
    }
}
