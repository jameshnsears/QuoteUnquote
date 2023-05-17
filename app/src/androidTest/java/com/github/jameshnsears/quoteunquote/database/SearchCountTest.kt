package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchCountTest : QuoteUnquoteModelUtility() {
    @Test
    fun getSearchQuotations() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "aaa", "zzz1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "AAA", "ZZZ2"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "bbb", "bbbaaa"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertEquals(
            3,
            quoteUnquoteModelDouble.getSearchQuotations(
                "aaa",
                false,
            ).size,
        )

        assertEquals(
            1,
            quoteUnquoteModelDouble.getSearchQuotations(
                "bBb",
                false,
            ).size,
        )
    }

    @Test
    fun countSearchText() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "aaa", "zzz1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "AAA", "ZZZ2"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "bbb", "bbbaAA"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertTrue(
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "aaa",
                false,
            ) ==
                quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                    "aaa",
                    false,
                ),
        )
    }
}
