package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WidgetToolbarPreviousTest : QuoteUnquoteModelUtility() {
    @Test
    fun erase() {
        assertEquals(
            0,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3456789")

        databaseRepositoryDouble.erase(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            0,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    @Test
    fun getQuotationPositionInPrevious() {
        pressNextSequentialFourTimes()

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3456789")

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )

        assertEquals(
            4,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, quotationsPreferences
            )
        )

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "7a36e553")

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, quotationsPreferences
            )
        )
    }

    private fun pressNextSequentialFourTimes() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        assertEquals(4, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))
    }

    @Test
    fun noMoreNext() {
        insertQuotationTestData02()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        pressNext(quotationsPreferences, "d2345678", "1/3", false)
        pressNext(quotationsPreferences, "d3456789", "2/3", false)
        pressPrevious(quotationsPreferences, "d2345678", "1/3")
        pressNext(quotationsPreferences, "d3456789", "2/3", false)
        pressNext(quotationsPreferences, "d4567890", "3/3", false)

        pressNext(quotationsPreferences, "d4567890", "3/3", false)
        pressPrevious(quotationsPreferences, "d3456789", "2/3")
    }

    private fun pressPrevious(
        quotationsPreferences: QuotationsPreferences,
        digestIfExpected: String,
        expectedPosition: String,
    ) {
        quoteUnquoteModelDouble.markAsCurrentPrevious(WidgetIdHelper.WIDGET_ID_01)

        if (digestIfExpected != "") {
            assertEquals(
                digestIfExpected,
                quoteUnquoteModelDouble.getCurrentQuotation(
                    WidgetIdHelper.WIDGET_ID_01
                )?.digest
            )
        }

        assertEquals(
            expectedPosition,
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01,
                quotationsPreferences
            )
        )
    }

    private fun pressNext(
        quotationsPreferences: QuotationsPreferences,
        digestIfExpected: String,
        expectedPosition: String,
        random: Boolean
    ) {
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, random)

        if (digestIfExpected != "") {
            assertEquals(
                digestIfExpected,
                quoteUnquoteModelDouble.getCurrentQuotation(
                    WidgetIdHelper.WIDGET_ID_01
                )?.digest
            )
        }
        assertEquals(
            expectedPosition,
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01, quotationsPreferences
            )
        )
    }

    @Test
    fun lastNextRandom() {
        insertQuotationTestData02()

        val quotationsPreferences =
            QuotationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        quotationsPreferences.contentSelection = ContentSelection.ALL

        pressNext(quotationsPreferences, "", "1/3", true)
        pressNext(quotationsPreferences, "", "2/3", true)
        pressPrevious(quotationsPreferences, "", "1/3")
        pressNext(quotationsPreferences, "", "3/3", true)
        pressPrevious(quotationsPreferences, "", "2/3")
        pressNext(quotationsPreferences, "", "3/3", true)
        pressNext(quotationsPreferences, "", "3/3", true)
    }
}
