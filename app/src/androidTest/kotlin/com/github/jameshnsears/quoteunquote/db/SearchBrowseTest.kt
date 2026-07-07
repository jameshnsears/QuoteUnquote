package com.github.jameshnsears.quoteunquote.db

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SearchBrowseTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionInPreviousAuthor() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertThat(
            quoteUnquoteModelDouble
                .getSearchQuotations(
                    "a2",
                    true,
                ).size,
            equalTo(0),
        )

        assertThat(
            quoteUnquoteModelDouble
                .getSearchQuotations(
                    "a2",
                    false,
                ).size,
            equalTo(3),
        )

        assertThat(
            quoteUnquoteModelDouble
                .getSearchQuotations(
                    "q1",
                    false,
                ).size,
            equalTo(4),
        )
    }

    @Test
    fun countQuotationWithSearchText() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "a2",
                false,
            ),
            equalTo(3),
        )

        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText(
                "a2",
                true,
            ),
            equalTo(0),
        )
    }

    @Test
    fun countQuotationWithSearchRegEx() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        QuotationsPreferences(
            WidgetIdHelper.WIDGET_ID_01,
            context,
        )

        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                "^a",
                false,
            ),
            equalTo(7),
        )

        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, "d2345678")
        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, "d6789012")

        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchTextRegEx(
                "^a2",
                true,
            ),
            equalTo(2),
        )
    }
}
