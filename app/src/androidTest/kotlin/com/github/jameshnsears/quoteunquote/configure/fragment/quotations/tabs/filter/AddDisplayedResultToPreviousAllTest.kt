package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AddDisplayedResultToPreviousAllTest : QuoteUnquoteModelUtility() {
    @Test
    fun doNotAddResultToPreviousAll() {
        quoteUnquoteModelDouble.setUseInternalDatabase(true)

        setupAuthorWithAddToPreviousAll(false)

        setupSearchWithAddToPreviousAll(false)

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(1),
        )

        assertThat(
            databaseRepositoryDouble
                .getPreviousDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quoteUnquoteModelDouble
                        .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                        .contentSelectionAllExclusion,
                ).get(index = 0),
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )
    }

    @Test
    fun addResultToPreviousAll() {
        quoteUnquoteModelDouble.setUseInternalDatabase(true)

        setupAuthorWithAddToPreviousAll(true)

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(2),
        )

        assertThat(
            databaseRepositoryDouble
                .getPreviousDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quoteUnquoteModelDouble
                        .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                        .contentSelectionAllExclusion,
                ).get(index = 0),
            equalTo("d2345678"),
        )

        assertThat(
            databaseRepositoryDouble
                .getPreviousDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quoteUnquoteModelDouble
                        .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                        .contentSelectionAllExclusion,
                ).get(index = 1),
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )

        setupSearchWithAddToPreviousAll(true)

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(3),
        )

        assertThat(
            databaseRepositoryDouble
                .getPreviousDigests(
                    true,
                    WidgetIdHelper.WIDGET_ID_01,
                    ContentSelection.ALL,
                    quoteUnquoteModelDouble
                        .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                        .contentSelectionAllExclusion,
                ).get(index = 0),
            equalTo("d3456789"),
        )
    }

    private fun setupAuthorWithAddToPreviousAll(contentAddToPrevious: Boolean) {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(0),
        )
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentAddToPreviousAll = contentAddToPrevious
        quotationsPreferences.contentSelection = ContentSelection.AUTHOR
        quotationsPreferences.contentSelectionAuthor = "a2"

        // d2
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01),
            equalTo(1),
        )
    }

    private fun setupSearchWithAddToPreviousAll(contentAddToPrevious: Boolean) {
        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentAddToPreviousAll = contentAddToPrevious
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q3"

        // d3
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01),
            equalTo(1),
        )
    }
}
