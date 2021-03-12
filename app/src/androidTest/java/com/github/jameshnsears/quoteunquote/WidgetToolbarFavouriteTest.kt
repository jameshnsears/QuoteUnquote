package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetToolbarFavouriteTest : QuoteUnquoteModelUtility() {
    @Test
    fun makeFavourite() {
        insertQuotationTestData01()

        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
        markDefaultQuotationAsFavourite()
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01, DatabaseRepository.DEFAULT_QUOTATION_DIGEST
        )

        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
    }

    @Test
    fun makeFavourites() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        val expectedDigestsList: MutableList<String> = ArrayList()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        // press 'favourite'
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        var digest = quoteUnquoteModelDouble.getNextQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
        ).digest

        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, digest.toString())

        expectedDigestsList.add(digest.toString())

        // press 'next'
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        // press 'favourite'
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        digest = quoteUnquoteModelDouble.getNextQuotation(
            WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL
        ).digest

        quoteUnquoteModelDouble.toggleFavourite(WidgetIdHelper.WIDGET_ID_01, digest.toString())

        expectedDigestsList.add(digest.toString())

        assertEquals(2, quoteUnquoteModelDouble.countFavourites())

        // the database returns in prior order
        expectedDigestsList.reverse()
        assertEquals(expectedDigestsList, quoteUnquoteModelDouble.favourites)
    }
}
