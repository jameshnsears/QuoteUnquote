package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchBrowseTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionInPreviousAuthor() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertEquals(
            0,
            quoteUnquoteModelDouble.getSearchQuotations(
                "a2",
                true,
            ).size,
        )

        assertEquals(
            3,
            quoteUnquoteModelDouble.getSearchQuotations(
                "a2",
                false,
            ).size,
        )

        assertEquals(
            4,
            quoteUnquoteModelDouble.getSearchQuotations(
                "q1",
                false,
            ).size,
        )
    }

    @Test
    fun countQuotationWithSearchText() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertEquals(
            3,
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "a2",
                false,
            ),
        )

        assertEquals(
            0,
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "a2",
                true,
            ),
        )
    }

    @Test
    fun countQuotationWithSearchRegEx() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertEquals(
            7,
            quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                "^a",
                false,
            ),
        )

        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, "d2345678")
        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, "d6789012")

        assertEquals(
            2,
            quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                "^a2",
                true,
            ),
        )
    }
}
