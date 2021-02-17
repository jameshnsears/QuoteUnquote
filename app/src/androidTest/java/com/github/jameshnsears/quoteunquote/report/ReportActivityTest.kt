package com.github.jameshnsears.quoteunquote.report

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.*
import org.junit.Test

class ReportActivityTest : QuoteUnquoteModelUtility() {
    @Test
    fun reportQuotation() {
        insertQuotationsTestData01()
        setDefaultQuotationAsPreviousAll(WidgetIdHelper.INSTANCE_01_WIDGET_ID)

        assertEquals(ContentSelection.ALL, quoteUnquoteModelDouble.selectedContentType(1))
        assertFalse(quoteUnquoteModelDouble.isReported(1))

        quoteUnquoteModelDouble.reportQuotation(1)

        assertTrue(quoteUnquoteModelDouble.isReported(1))
    }
}
