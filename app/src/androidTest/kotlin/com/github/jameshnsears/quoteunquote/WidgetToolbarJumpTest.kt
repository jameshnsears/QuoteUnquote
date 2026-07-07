package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class WidgetToolbarJumpTest : QuoteUnquoteModelUtility() {
    @Test
    fun onReceiveToolbarPressedJump() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d1234567"),
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d2345678"),
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d1234567"),
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )

        // jump
        quoteUnquoteModelDouble.markAsCurrentLastPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d2345678"),
        )
    }
}
