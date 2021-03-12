package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WidgetToolbarPreviousTest : QuoteUnquoteModelUtility() {
    @Test
    fun getPreviousQuotation() {
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
    }

    @Test
    fun markAsCurrent() {
        pressNextSequentialFourTimes()

        assertEquals(
                0,
                databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3")

        assertEquals(
                1,
                databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))


        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d2")

        assertEquals(
                1,
                databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

        assertEquals(
                "d2",
                databaseRepositoryDouble.getCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

    }

    @Test
    fun deleteAll() {
        assertEquals(
                0,
                databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3")

        databaseRepositoryDouble.deleteCurrent()

        assertEquals(
                0,
                databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))


        /*
        getPreviousQuotation = DONE

        markAsCurrent = DONE

        getQuotationPositionInPrevious
        = @ n/n
            = in databaseRepository API

        setNextQuotation
        = uses markAsCurrent

        getNextQuotation -> getCurrentQuotation

        tests to include previous quotes in ContentSelection.FAVOURITES + AUTHOR + SEARCH
        */
    }

    private fun pressNextSequentialFourTimes() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(4, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))
    }
}
