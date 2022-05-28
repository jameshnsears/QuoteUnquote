package com.github.jameshnsears.quoteunquote.configure.fragment.quotations

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase
import org.junit.Test

class AddReadResultToPreviousAllTest : QuoteUnquoteModelUtility() {
    @Test
    fun doNotAddResultToPreviousAll() {
        setupAuthorWithAddToPreviousAll(false)

        setupSearchWithAddToPreviousAll(false)

        TestCase.assertEquals(
            1,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        TestCase.assertEquals(
            "7a36e553",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL
            ).get(index = 0)
        )
    }

    @Test
    fun addResultToPreviousAll() {
        setupAuthorWithAddToPreviousAll(true)

        TestCase.assertEquals(
            2,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        TestCase.assertEquals(
            "d2345678",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL
            ).get(index = 0)
        )

        TestCase.assertEquals(
            "7a36e553",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL
            ).get(index = 1)
        )

        setupSearchWithAddToPreviousAll(true)

        TestCase.assertEquals(
            3,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )

        TestCase.assertEquals(
            "d3456789",
            databaseRepositoryDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL
            ).get(index = 0)
        )
    }

    fun setupAuthorWithAddToPreviousAll(contentAddToPrevious: Boolean) {
        insertQuotationTestData01()
        insertQuotationTestData02()

        TestCase.assertEquals(
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

        TestCase.assertEquals(
            1,
            quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    fun setupSearchWithAddToPreviousAll(contentAddToPrevious: Boolean) {
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

        TestCase.assertEquals(
            1,
            quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01)
        )
    }
}
