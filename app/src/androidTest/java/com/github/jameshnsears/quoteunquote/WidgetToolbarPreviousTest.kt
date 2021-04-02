package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
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

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3")

        databaseRepositoryDouble.erase(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(
            0,
            databaseRepositoryDouble.countCurrent(WidgetIdHelper.WIDGET_ID_01)
        )
    }

    @Test
    fun getQuotationPositionInPrevious() {
        pressNextSequentialFourTimes()

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "d3")

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)

        assertEquals(
            4,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, "1624c314")

        assertEquals(
            1,
            databaseRepositoryDouble.positionInPrevious(
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
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

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        pressNext(contentPreferences, "d2", "@ 1/3", false)
        pressNext(contentPreferences, "d3", "@ 2/3", false)
        pressPrevious(contentPreferences, "d2", "@ 1/3")
        pressNext(contentPreferences, "d3", "@ 2/3", false)
        pressNext(contentPreferences, "d4", "@ 3/3", false)

        pressNext(contentPreferences, "d4", "@ 3/3", false)
        pressPrevious(contentPreferences, "d3", "@ 2/3")
    }

    private fun pressPrevious(
        contentPreferences: ContentPreferences,
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
            quoteUnquoteModelDouble.getCurrentPosition(WidgetIdHelper.WIDGET_ID_01, contentPreferences)
        )
    }

    private fun pressNext(
        contentPreferences: ContentPreferences,
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
                WidgetIdHelper.WIDGET_ID_01, contentPreferences
            )
        )
    }

    @Test
    fun lastNextRandom() {
        insertQuotationTestData02()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        pressNext(contentPreferences, "", "@ 1/3", true)
        pressNext(contentPreferences, "", "@ 2/3", true)
        pressPrevious(contentPreferences, "", "@ 1/3")
        pressNext(contentPreferences, "", "@ 3/3", true)
        pressPrevious(contentPreferences, "", "@ 2/3")
        pressNext(contentPreferences, "", "@ 3/3", true)
        pressNext(contentPreferences, "", "@ 3/3", true)
    }
}
