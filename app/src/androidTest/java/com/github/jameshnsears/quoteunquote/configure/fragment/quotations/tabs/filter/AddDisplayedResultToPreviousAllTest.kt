package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class AddDisplayedResultToPreviousAllTest : QuoteUnquoteModelUtility() {
    @Test
    fun doNotAddResultToPreviousAll() {
        DatabaseRepositoryDouble.useInternalDatabase = true

        setupAuthorWithAddToPreviousAll(false)

        setupSearchWithAddToPreviousAll(false)

        assertEquals(
            1,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01).contentSelectionAllExclusion
            ).get(index = 0)
        )
    }

    @Test
    fun addResultToPreviousAll() {
        DatabaseRepositoryDouble.useInternalDatabase = true

        setupAuthorWithAddToPreviousAll(true)

        assertEquals(
            2,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        assertEquals(
            "d2345678",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01).contentSelectionAllExclusion
            ).get(index = 0)
        )

        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01).contentSelectionAllExclusion
            ).get(index = 1)
        )

        setupSearchWithAddToPreviousAll(true)

        assertEquals(
            3,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        assertEquals(
            "d3456789",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01).contentSelectionAllExclusion
            ).get(index = 0)
        )
    }

    private fun setupAuthorWithAddToPreviousAll(contentAddToPrevious: Boolean) {
        insertQuotationTestData01()
        insertQuotationTestData02()

        assertEquals(
            0,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentAddToPreviousAll = contentAddToPrevious
        quotationsPreferences.contentSelection = ContentSelection.AUTHOR
        quotationsPreferences.contentSelectionAuthor = "a2"

        // d2
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    private fun setupSearchWithAddToPreviousAll(contentAddToPrevious: Boolean) {
        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentAddToPreviousAll = contentAddToPrevious
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q3"

        // d3
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(
            1,
            quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01)
        )
    }
}
