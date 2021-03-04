package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class WidgetToolbarNextTest : QuoteUnquoteModelUtility() {
    @Test
    fun deadDigestFromFavouriteReceive() {
        assertNull(quoteUnquoteModelDouble.databaseRepository?.getQuotation("blah"))
    }

    @Test
    fun nextAll() {
        moveThroughContentSelection(false)
    }

    @Test
    fun nextAllRandom() {
        moveThroughContentSelection(true)
    }

    private fun moveThroughContentSelection(randomNext: Boolean) {
        insertQuotationTestData01()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        assertEquals(
            0,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, randomNext)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, randomNext)

        assertEquals(
            2,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    @Test(expected = NoNextQuotationAvailableException::class)
    fun noMoreQuotations() {
        moveThroughContentSelection(false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
    }

    @Test
    fun nextAuthor() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthorName = "a2"

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        try {
            quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
            fail()
        } catch (exception: NoNextQuotationAvailableException) {
            // should be thrown
        }

        assertEquals(3, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01))

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelDouble.resetPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.AUTHOR)
        assertEquals(0, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01))
    }

    @Test
    fun nextFavourite() {
        insertQuotationTestData01()

        markDefaultQuotationAsFavourite()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            DatabaseRepository.DEFAULT_QUOTATION_DIGEST,
            quoteUnquoteModelDouble.getNextQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES
            ).digest
        )
    }

    @Test
    fun nextSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.SEARCH
        contentPreferences.contentSelectionSearchText = "q1"

        try {
            for (i in 1..4) {
                quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
            }

            quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
            fail()
        } catch (exception: NoNextQuotationAvailableException) {
            assertEquals(
                4,
                quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01)
            )
        }
    }
}
