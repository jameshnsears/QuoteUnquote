package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PackageReplacedHelperTest : QuoteUnquoteModelUtility() {
    @Test
    fun alignHistoryWithQuotations() {
        insertQuotationTestData01()

        confirmPreviousIsEmpty()

        createDigestsNotInQuotationsDatabase()

        val packageReplacedHelper = PackageReplacedHelper(WidgetIdHelper.WIDGET_ID_01, context)
        packageReplacedHelper.alignHistoryWithQuotations(quoteUnquoteModelDouble)

        confirmPreviousIsEmpty()
    }

    private fun confirmPreviousIsEmpty() {
        assertFalse(
            databaseRepositoryDouble.getPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL
            ).contains("all-0")
        )

        assertEquals(
            0,
            databaseRepositoryDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.AUTHOR,
            )
        )

        assertEquals(
            0,
            databaseRepositoryDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.SEARCH,
            )
        )

        assertEquals(
            0,
            databaseRepositoryDouble.countFavourites().blockingGet()
        )
    }

    private fun createDigestsNotInQuotationsDatabase() {
        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "all-0"
        )
        assertEquals(
            1,
            databaseRepositoryDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            )
        )

        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.AUTHOR,
            "author-0"
        )
        assertEquals(
            1,
            databaseRepositoryDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.AUTHOR,
            )
        )

        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.SEARCH,
            "search-0"
        )
        assertEquals(
            1,
            databaseRepositoryDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.SEARCH,
            )
        )

        databaseRepositoryDouble.markAsFavourite("favourite-0")
        assertEquals(1, databaseRepositoryDouble.countFavourites().blockingGet())
    }
}
