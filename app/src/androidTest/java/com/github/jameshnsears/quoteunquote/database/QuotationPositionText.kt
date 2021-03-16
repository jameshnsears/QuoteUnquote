package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuotationPositionText : QuoteUnquoteModelUtility() {
    @Test
    fun getPreviousNextCountsAll() {
        insertQuotationTestData01()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        assertEquals(
            "@ 0/2",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            "@ 1/2",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun getPreviousNextCountsFavourites() {
        insertQuotationTestData01()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            "@ 0/0",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            "@ 0/1",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            "@ 0/2",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "@ 1/2",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            "@ 2/2",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
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

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthor = "a2"


        assertEquals(
            "@ 0/3",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelectionAuthor = "a2"

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            "@ 1/3",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun getPreviousNextCountsSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.SEARCH
        contentPreferences.contentSelectionSearch = "q1"

        assertEquals(
            "@ 0/4",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        quoteUnquoteModelDouble.setNextQuotation(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            "@ 1/4",
            databaseRepositoryDouble.getQuotationPosition(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }
}
