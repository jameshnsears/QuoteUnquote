package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class WidgetToolbarPreviousTest : QuoteUnquoteModelUtility() {
    @Test
    fun pressPreviousButton() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        pressNextSequentialFourTimes()

        val currentQuotation = quoteUnquoteModelDouble.getNextQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        assertEquals("d3", currentQuotation.digest)

        val allPreviousDigests = quoteUnquoteModelDouble.getAllPrevious(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        assertEquals(4, allPreviousDigests.size)

        assertEquals("d3", allPreviousDigests[0])
        assertEquals("d2", allPreviousDigests[1])
        assertEquals("d1", allPreviousDigests[2])
        assertEquals("1624c314", allPreviousDigests[3])

        assertEquals("d2", quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3")?.digest)

        assertEquals("d1", quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d2")?.digest)

        assertEquals("1624c314", quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d1")?.digest)

        assertEquals("1624c314", quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "1624c314")?.digest)

        /*
        getPreviousQuotation = DONE

        setCurrentQuotation
        = where does the value go?
        = own entity in History database?

        setNextQuotation
        = uses setCurrentQuotation

        getNextQuotation -> getCurrentQuotation

        getQuotationPositionInPrevious
        = @ n/n
            = in databaseRepository API

         test to go back in ContentSelection.FAVOURITES + AUTHOR + SEARCH
        */
    }

    private fun pressNextSequentialFourTimes() {
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(4, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))
    }
}
