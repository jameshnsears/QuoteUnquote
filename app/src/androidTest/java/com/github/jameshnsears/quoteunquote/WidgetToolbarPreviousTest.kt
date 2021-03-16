package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WidgetToolbarPreviousTest : QuoteUnquoteModelUtility() {
    @Test
    fun getPreviousQuotation() {
        pressNextSequentialFourTimes()

        val allPreviousDigests = quoteUnquoteModelDouble.getAllPrevious(
            WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
        )
        assertEquals(4, allPreviousDigests.size)

        assertEquals("d3", allPreviousDigests[0])
        assertEquals("d2", allPreviousDigests[1])
        assertEquals("d1", allPreviousDigests[2])
        assertEquals("1624c314", allPreviousDigests[3])

        assertEquals(
            "d2",
            quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3"
            )?.digest
        )

        assertEquals(
            "d1",
            quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d2"
            )?.digest
        )

        assertEquals(
            "1624c314",
            quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d1"
            )?.digest
        )

        assertEquals(
            "1624c314",
            quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "1624c314"
            )?.digest
        )
    }

    @Test
    fun markAsCurrent() {
        pressNextSequentialFourTimes()

        assertEquals(
            1,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )

        assertEquals(
            "d3",
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest
        )
    }

    @Test
    fun erase() {
        assertEquals(
            0,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3")

        databaseRepositoryDouble.erase(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            0,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    @Test
    fun getQuotationPositionInPrevious() {
        pressNextSequentialFourTimes()

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3")

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        assertEquals(
            "@ 4/7",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "1624c314")

        assertEquals(
            "@ 1/7",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun getCurrentQuotation() {
        insertQuotationTestData01()

        val contentPrevious = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPrevious.contentSelection = ContentSelection.ALL

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
                "1624c314",
                quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL).digest)
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
