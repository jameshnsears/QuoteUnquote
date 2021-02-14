package com.github.jameshnsears.quoteunquote.report

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTest : QuoteUnquoteModelUtility() {
    @Test
    fun reportQuotation() {
        insertTestDataSet01()
        setDefaultQuotation()

        assertEquals(ContentSelection.ALL, quoteUnquoteModelDouble.selectedContentType(1))
        assertFalse(quoteUnquoteModelDouble.isReported(1))

        quoteUnquoteModelDouble.reportQuotation(1)

        assertTrue(quoteUnquoteModelDouble.isReported(1))
    }
}
