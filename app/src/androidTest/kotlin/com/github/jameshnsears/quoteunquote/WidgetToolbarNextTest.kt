package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class WidgetToolbarNextTest : QuoteUnquoteModelUtility() {
    @Test
    fun positionIndicatorNext() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        /*
        > next > next > next : 1624c314 > D1 > D2
        < back : D1
        > next = show position indicator : D2
        > next (digest never seen before)
         */

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(0),
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d1234567"),
        )

        assertThat(
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01,
            ),
            equalTo("d1234567"),
        )

        // next
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d2345678"),
        )

        assertThat(
            quoteUnquoteModelDouble.getLastPreviousDigest(
                WidgetIdHelper.WIDGET_ID_01,
            ),
            equalTo("d2345678"),
        )

        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d1234567"),
        )
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            not(
                equalTo(
                    quoteUnquoteModelDouble.getLastPreviousDigest(
                        WidgetIdHelper.WIDGET_ID_01,
                    ),
                ),
            ),
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo("d2345678"),
        )

        // if current quotation == last previous digest then we can show indicator
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo(
                quoteUnquoteModelDouble.getLastPreviousDigest(
                    WidgetIdHelper.WIDGET_ID_01,
                ),
            ),
        )
    }

    @Test
    fun deadDigestFromFavouriteReceive() {
        assertThat(quoteUnquoteModelDouble.databaseRepository?.getQuotation(true, "blah"), nullValue())
    }

    @Test
    fun nextAll() {
        moveThroughContentSelection(false)
    }

    @Test
    fun nextAllRandom() {
        moveThroughContentSelection(true)
    }

    private fun moveThroughContentSelection(randomNext: Boolean) {
        insertQuotationTestData01(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(0),
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, randomNext)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, randomNext)

        assertThat(
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01),
            equalTo(2),
        )
    }

    @Test
    fun nextAuthorWithRecycling() {
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

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01), equalTo(3))

        // recycle
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01), equalTo(1))

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelDouble.resetPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.AUTHOR)
        assertThat(quoteUnquoteModelDouble.countPreviousAuthor(WidgetIdHelper.WIDGET_ID_01), equalTo(0))
    }

    @Test
    fun nextFavourite() {
        insertQuotationTestData01(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

        assertThat(
            databaseRepositoryDouble.countNext(true, quotationsPreferences),
            equalTo(0),
        )

        markDefaultQuotationAsFavourite(true)

        assertThat(
            databaseRepositoryDouble.countNext(true, quotationsPreferences),
            equalTo(1),
        )

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            quoteUnquoteModelDouble
                .getCurrentQuotation(
                    WidgetIdHelper.WIDGET_ID_01,
                )?.digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )
    }

    @Test
    fun nextSearch() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), equalTo(0))

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.SEARCH
        quotationsPreferences.contentSelectionSearch = "q1"

        for (i in 1..4) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        }

        // recycle
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(
            quoteUnquoteModelDouble.countPreviousSearch(WidgetIdHelper.WIDGET_ID_01),
            equalTo(1),
        )
    }
}
