package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseRepositoryTest : QuoteUnquoteModelUtility() {
    @Test
    fun getPreviousNextCountsAll() {
        insertQuotationTestData01()
        assertEquals("@ 0/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals("@ 1/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))
    }

    @Test
    fun getPreviousNextCountsFavourites() {
        insertQuotationTestData01()
        assertEquals("@ 0/0", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES))

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        markNextQuotationAsFavourite(ContentSelection.ALL)
        assertEquals("@ 0/1", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES))

        markNextQuotationAsFavourite(ContentSelection.ALL)
        assertEquals("@ 0/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES))

        contentPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals("@ 1/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES))

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals("@ 2/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES))
    }

    private fun markNextQuotationAsFavourite(contentSelection: ContentSelection) {
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        val quotationEntity = databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        databaseRepositoryDouble.markAsFavourite(quotationEntity.digest)
    }

//    @Test
//    fun getPreviousNextCountsAuthor() {
//
//    }

//    @Test
//    fun getPreviousNextCountsSearch() {
//
//    }
}
