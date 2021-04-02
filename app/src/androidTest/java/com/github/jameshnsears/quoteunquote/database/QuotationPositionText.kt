package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class QuotationPositionText : QuoteUnquoteModelUtility() {
    @Test
    fun positionInPreviousAll() {
        insertQuotationTestData01()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun positionInPreviousFavourites() {
        insertQuotationTestData01()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            2,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    private fun markNextQuotationAsFavourite(contentSelection: ContentSelection) {
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        val quotationEntity = databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        databaseRepositoryDouble.markAsFavourite(quotationEntity.digest)
    }

    @Test
    fun positionInPreviousAuthor() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthor = "a2"

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        contentPreferences.contentSelectionAuthor = "a2"

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun positionInPreviousSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.SEARCH
        contentPreferences.contentSelectionSearch = "q1"

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }
}
