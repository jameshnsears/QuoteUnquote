package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetToolbarJumpTest : QuoteUnquoteModelUtility() {
    @Test
    fun onReceiveToolbarPressedJump() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )

        // jump
        quoteUnquoteModelDouble.markAsCurrentLastPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
        )
    }
}
