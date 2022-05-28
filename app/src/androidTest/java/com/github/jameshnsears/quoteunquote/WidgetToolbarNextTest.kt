package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetToolbarNextTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionIndicatorNext() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        /*
        > next > next > next : 7a36e553 > D1 > D2
        < back : D1
        > next = show position indicator : D2
        > next (digest never seen before)
         */

        assertEquals(
            0,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "7a36e553",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
            )
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
            )
        )

        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertEquals(
            "d1234567",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )
        assertNotEquals(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
            )
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "d2345678",
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )

        // if current quotation == last previous digest then we can show indicator
        assertEquals(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
            )
        )
    }

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

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        assertEquals(
            0,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, randomNext)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, randomNext)

        assertEquals(
            2,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    @Test
    fun nextAuthor() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.AUTHOR
        quotationsPreferences.contentSelectionAuthor = "a2"

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(3, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01))

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelDouble.resetPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.AUTHOR)
        assertEquals(0, quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01))
    }

    @Test
    fun nextFavourite() {
        insertQuotationTestData01()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.countNext(quotationsPreferences)
        )

        markDefaultQuotationAsFavourite()

        assertEquals(
            1,
            databaseRepositoryDouble.countNext(quotationsPreferences)
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            quoteUnquoteModelDouble.getCurrentQuotation(
                WidgetIdHelper.WIDGET_ID_01
            )?.digest
        )
    }

    @Test
    fun nextSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q1"

        for (i in 1..4) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        }

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            4,
            quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01)
        )
    }
}
