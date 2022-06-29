package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class QuotationPositionTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionInPreviousAll() {
        insertQuotationTestData01()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )
    }

    @Test
    fun positionInPreviousFavourites() {
        insertQuotationTestData01()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quotationsPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quotationsPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertEquals(
            2,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )
    }

    private fun markNextQuotationAsFavourite(contentSelection: ContentSelection) {
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        val quotationEntity =
            databaseRepositoryDouble.getNextQuotation(WidgetIdHelper.WIDGET_ID_01, contentSelection)
        databaseRepositoryDouble.markAsFavourite(quotationEntity.digest)
    }

    @Test
    fun positionInPreviousAuthor() {
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

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quotationsPreferences.contentSelectionAuthor = "a2"

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )
    }

    @Test
    fun positionInPreviousSearch() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q1"

        assertEquals(
            0,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )
    }
}
