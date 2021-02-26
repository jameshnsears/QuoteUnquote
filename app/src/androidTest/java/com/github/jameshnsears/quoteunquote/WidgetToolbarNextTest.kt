package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class WidgetToolbarNextTest : QuoteUnquoteModelUtility() {
    @Test
    fun deadDigestFromFavouriteReceive() {
        assertNull(quoteUnquoteModelDouble.databaseRepository?.getQuotation("blah"))
    }

    @Test
    fun nextAll() {
        moveThroughContentSelection(ContentSelection.ALL, false)
    }

    @Test
    fun nextAllRandom() {
        moveThroughContentSelection(ContentSelection.ALL, true)
    }

    private fun moveThroughContentSelection(contentSelection: ContentSelection, randomNext: Boolean) {
        insertQuotationsTestData01()

        assertEquals(
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, contentSelection)
        )

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, contentSelection, randomNext)
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, contentSelection, randomNext)

        assertEquals(
                2,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, contentSelection)
        )
    }

    @Test(expected = NoNextQuotationAvailableException::class)
    fun noMoreQuotations() {
        moveThroughContentSelection(ContentSelection.ALL, false)

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false)
    }

    @Test
    fun nextAuthor() {
        insertQuotationsTestData01()
        insertQuotationsTestData02()
        insertQuotationsTestData03()

        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID))

        val contentPreferences = ContentPreferences(WidgetIdHelper.INSTANCE_01_WIDGET_ID, context)
        contentPreferences.contentSelectionAuthorName = "a2"

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.AUTHOR, false)
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.AUTHOR, false)
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.AUTHOR, false)

        try {
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.AUTHOR, false)
            fail()
        } catch (exception: NoNextQuotationAvailableException) {
            // should be thrown
        }

        assertEquals(3, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.INSTANCE_01_WIDGET_ID))

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelDouble.resetPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.AUTHOR)
        assertEquals(0, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.INSTANCE_01_WIDGET_ID))
    }

    @Test
    fun nextFavourite() {
        insertQuotationsTestData01()

        markDefaultQuotationAsFavourite()

        assertEquals(
                DatabaseRepository.DEFAULT_QUOTATION_DIGEST,
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.FAVOURITES
                )?.digest
        )
    }

    @Test
    fun nextSearch() {
        insertQuotationsTestData01()
        insertQuotationsTestData02()
        insertQuotationsTestData03()

        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID))

        val contentPreferences = ContentPreferences(WidgetIdHelper.INSTANCE_01_WIDGET_ID, context)
        contentPreferences.contentSelectionSearchText = "q1"

        try {
            for (i in 1..4) {
                quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.SEARCH, false)
            }

            quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.SEARCH, false)
            fail()
        } catch (exception: NoNextQuotationAvailableException) {
            assertSame(ContentSelection.SEARCH, exception.contentSelection)
        }

        assertEquals(
                4,
                quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        )
    }
}
