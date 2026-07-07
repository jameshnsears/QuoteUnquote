package com.github.jameshnsears.quoteunquote.db

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class QuotationPositionTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionInPreviousAll() {
        insertQuotationTestData01(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        setDefaultQuotationAll(true, WidgetIdHelper.WIDGET_ID_01)

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(1),
        )
    }

    @Test
    fun positionInPreviousFavourites() {
        insertQuotationTestData01(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        quotationsPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        quotationsPreferences.contentSelection = ContentSelection.ALL
        markNextQuotationAsFavourite(ContentSelection.ALL)

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(1),
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(2),
        )
    }

    private fun markNextQuotationAsFavourite(contentSelection: ContentSelection) {
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        databaseRepositoryDouble.getNextQuotation(true, WidgetIdHelper.WIDGET_ID_01, contentSelection)
        val quotationEntity =
            databaseRepositoryDouble.getNextQuotation(true, WidgetIdHelper.WIDGET_ID_01, contentSelection)
        databaseRepositoryDouble.markAsFavourite(true, quotationEntity.digest)
    }

    @Test
    fun positionInPreviousAuthor() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.AUTHOR
        quotationsPreferences.contentSelectionAuthor = "a2"

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        quotationsPreferences.contentSelectionAuthor = "a2"

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(1),
        )
    }

    @Test
    fun positionInPreviousSearch() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q1"

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(0),
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(1),
        )
    }
}
