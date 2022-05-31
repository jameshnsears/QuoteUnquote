package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase
import org.junit.Test

class WidgetToolbarJumpTest : QuoteUnquoteModelUtility() {
    @Test
    fun onReceiveToolbarPressedJump() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        TestCase.assertEquals(
            "7a36e553",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        TestCase.assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        TestCase.assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        TestCase.assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // previous
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        TestCase.assertEquals(
            "7a36e553",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // jump
        quoteUnquoteModelDouble.markAsCurrentLastPrevious(WidgetIdHelper.WIDGET_ID_01)
        TestCase.assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )
    }
}
