package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class PreviousNextCountsTest : QuoteUnquoteModelUtility() {
    @Test
    fun getPreviousNextCountsAll() {
        insertQuotationTestData01()
        assertEquals(
            "@ 0/2",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, null
            )
        )

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            "@ 1/2",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, null
            )
        )
    }

    @Test
    fun getPreviousNextCountsFavourites() {
        insertQuotationTestData01()
        assertEquals(
            "@ 0/0",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES, null
            )
        )

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        markNextQuotationAsFavourite(ContentSelection.ALL)
        assertEquals(
            "@ 0/1",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES, null
            )
        )

        markNextQuotationAsFavourite(ContentSelection.ALL)
        assertEquals(
            "@ 0/2",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES, null
            )
        )

        contentPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "@ 1/2",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES, null
            )
        )

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "@ 2/2",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.FAVOURITES, null
            )
        )
    }

    private fun markNextQuotationAsFavourite(contentSelection: ContentSelection) {
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        val quotationEntity = databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        databaseRepositoryDouble.markAsFavourite(quotationEntity.digest)
    }

    @Test
    fun getPreviousNextCountsAuthor() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals(
            "@ 0/3",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.AUTHOR, "a2"
            )
        )

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthorName = "a2"

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            "@ 1/3",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.AUTHOR, "a2"
            )
        )
    }

    @Test
    fun getPreviousNextCountsSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals(
            "@ 0/4",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.SEARCH, "q1"
            )
        )

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.SEARCH
        contentPreferences.contentSelectionSearchText = "q1"

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            "@ 1/4",
            databaseRepositoryDouble.getPreviousNextCounts(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.SEARCH, "q1"
            )
        )
    }
}
