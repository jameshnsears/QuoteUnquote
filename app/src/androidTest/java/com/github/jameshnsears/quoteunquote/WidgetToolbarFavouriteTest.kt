package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

class WidgetToolbarFavouriteTest : QuoteUnquoteModelUtility() {
    @Test
    fun makeFavourite() {
        insertQuotationsTestData01()

        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
        markDefaultQuotationAsFavourite()
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)

        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, DatabaseRepository.DEFAULT_QUOTATION_DIGEST)

        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
    }

    @Test
    @Throws(NoNextQuotationAvailableException::class)
    fun makeFavourites() {
        insertQuotationsTestData01()
        insertQuotationsTestData02()

        val expectedDigestsList: MutableList<String> = ArrayList()

        // press 'favourite'
        quoteUnquoteModelDouble.setNext(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false)
        var digest = quoteUnquoteModelDouble.getNext(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL)?.digest
        if (digest != null) {
            quoteUnquoteModelDouble.toggleFavourite(
                    WidgetIdHelper.INSTANCE_01_WIDGET_ID, digest)
            expectedDigestsList.add(digest)
        }

        // press 'next'
        quoteUnquoteModelDouble.setNext(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false)

        // press 'favourite'
        quoteUnquoteModelDouble.setNext(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false)
        digest = quoteUnquoteModelDouble.getNext(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL)?.digest
        if (digest != null) {
            quoteUnquoteModelDouble.toggleFavourite(
                    WidgetIdHelper.INSTANCE_01_WIDGET_ID, digest)
            expectedDigestsList.add(digest)
        }

        assertEquals(2, quoteUnquoteModelDouble.countFavourites())

        // the database returns in prior order
        Collections.reverse(expectedDigestsList)
        assertEquals(expectedDigestsList, quoteUnquoteModelDouble.favourites)
    }
}