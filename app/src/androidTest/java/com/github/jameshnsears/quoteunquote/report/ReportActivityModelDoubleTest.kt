package com.github.jameshnsears.quoteunquote.report

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportActivityModelDoubleTest : QuoteUnquoteModelUtility() {
    @Test
    fun reportQuotation() {
        insertQuotationTestData01()
        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertFalse(quoteUnquoteModelDouble.isReported(WidgetIdHelper.WIDGET_ID_01))

        quoteUnquoteModelDouble.markAsReported(WidgetIdHelper.WIDGET_ID_01)

        assertTrue(quoteUnquoteModelDouble.isReported(WidgetIdHelper.WIDGET_ID_01))
    }
}
