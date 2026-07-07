package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class WidgetToolbarPreviousTest : QuoteUnquoteModelUtility() {
    @Test
    fun erase() {
        assertThat(
            databaseRepositoryDouble.countCurrent(
                true,
                WidgetIdHelper.WIDGET_ID_01,
            ),
            equalTo(0),
        )

        databaseRepositoryDouble.markAsCurrent(true, WidgetIdHelper.WIDGET_ID_01, "d3456789")

        databaseRepositoryDouble.erase(true, WidgetIdHelper.WIDGET_ID_01)

        assertThat(
            databaseRepositoryDouble.countCurrent(
                true,
                WidgetIdHelper.WIDGET_ID_01,
            ),
            equalTo(0),
        )
    }

    @Test
    fun getQuotationPositionInPrevious() {
        pressNextSequentialFourTimes()

        databaseRepositoryDouble.markAsCurrent(true, WidgetIdHelper.WIDGET_ID_01, "d3456789")

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(4),
        )

        databaseRepositoryDouble.markAsCurrent(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            DatabaseRepository.getDefaultQuotationDigest(true),
        )

        assertThat(
            databaseRepositoryDouble.findPositionInPrevious(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(1),
        )
    }

    private fun pressNextSequentialFourTimes() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), equalTo(4))
    }

    @Test
    fun nextRecycles() {
        insertQuotationTestData02(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        pressNext(quotationsPreferences, "d2345678", "1/3", false)
        pressNext(quotationsPreferences, "d3456789", "2/3", false)
        pressPrevious(quotationsPreferences, "d2345678", "1/3")
        pressNext(quotationsPreferences, "d3456789", "2/3", false)
        pressNext(quotationsPreferences, "d4567890", "3/3", false)
        // recycles
        pressNext(quotationsPreferences, "d2345678", "1/3", false)
        pressPrevious(quotationsPreferences, "d2345678", "1/3")
    }

    private fun pressPrevious(
        quotationsPreferences: QuotationsPreferences,
        digestIfExpected: String,
        expectedPosition: String,
    ) {
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)

        if (digestIfExpected != "") {
            assertThat(
                quoteUnquoteModelDouble
                    .getCurrentQuotation(
                        WidgetIdHelper.WIDGET_ID_01,
                    )?.digest,
                equalTo(digestIfExpected),
            )
        }

        assertThat(
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(expectedPosition),
        )
    }

    private fun pressNext(
        quotationsPreferences: QuotationsPreferences,
        digestIfExpected: String,
        expectedPosition: String,
        random: Boolean,
    ) {
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, random)

        if (digestIfExpected != "") {
            assertThat(
                quoteUnquoteModelDouble
                    .getCurrentQuotation(
                        WidgetIdHelper.WIDGET_ID_01,
                    )?.digest,
                equalTo(digestIfExpected),
            )
        }
        assertThat(
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences,
            ),
            equalTo(expectedPosition),
        )
    }

    @Test
    fun lastNextRandom() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context,
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        pressNext(quotationsPreferences, "", "1/5", true)
        pressNext(quotationsPreferences, "", "2/5", true)
        pressNext(quotationsPreferences, "", "3/5", true)
        pressNext(quotationsPreferences, "", "4/5", true)
        pressNext(quotationsPreferences, "", "5/5", true)

        pressNext(quotationsPreferences, "", "1/5", true)
        pressNext(quotationsPreferences, "", "2/5", true)
    }
}
