package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class WidgetToolbarRandomizationTest : QuoteUnquoteModelUtility() {
    val expectedAllInsertionOrder =
        mutableListOf(
            DatabaseRepository.getDefaultQuotationDigest(true),
            "d1234567",
            "d2345678",
            "d3456789",
            "d6789012",
            "d4567890",
            "d5678901",
        )

    @Before
    fun setup() {
        if (Timber.treeCount == 0) {
            Timber.plant(MethodLineLoggingTree())
        }

        databaseRepositoryDouble.eraseAllDatabsaes()
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)
        assertThat(
            quoteUnquoteModelDouble
                .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
                .blockingGet(),
            equalTo(7),
        )

        assertThat(
            quoteUnquoteModelDouble.countPrevious(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
            ),
            equalTo(0),
        )
    }

    @Test
    fun traverseContentSelectionSequentially() {
        assertInsertionOrder(expectedAllInsertionOrder)

        val expectedNextNextOrder =
            mutableListOf(
                DatabaseRepository.getDefaultQuotationDigest(true),
                "d1234567",
                "d2345678",
                "d3456789",
                "d6789012",
                "d4567890",
                "d5678901",
            )

        assertNextOrder(expectedNextNextOrder)

        // use the Next quotations
        for (
        i in 1..quoteUnquoteModelDouble
            .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
            .blockingGet()
        ) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        }

        val actualPreviousOrder =
            quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                    .contentSelectionAllExclusion,
            )
        actualPreviousOrder?.reverse()

        assertThat(actualPreviousOrder, equalTo(expectedNextNextOrder))
    }

    private fun assertInsertionOrder(expectedAllQuotationsOrder: MutableList<String>) {
        val actualAllQuotationsOrder = mutableListOf<String>()
        for (quotationEntity in quoteUnquoteModelDouble.allQuotations) {
            actualAllQuotationsOrder.add(quotationEntity.digest)
        }
        assertThat(actualAllQuotationsOrder, equalTo(expectedAllQuotationsOrder))
    }

    private fun assertNextOrder(expectedAllQuotationsOrder: MutableList<String>) {
        val nextAllDigests = databaseRepositoryDouble.getNextAllDigests(true)

        assertThat(
            nextAllDigests.size,
            equalTo(expectedAllQuotationsOrder.size),
        )
    }

    @Test
    fun traverseContentSelectionRandomly() {
        for (
        i in 1..quoteUnquoteModelDouble
            .countAllMinusExclusions(WidgetIdHelper.WIDGET_ID_01)
            .blockingGet()
        ) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, true)
        }

        val actualPreviousOrder =
            quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
                true,
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                quoteUnquoteModelDouble
                    .getContentPreferences(WidgetIdHelper.WIDGET_ID_01)
                    .contentSelectionAllExclusion,
            )
        actualPreviousOrder?.reverse()

        assertThat(actualPreviousOrder == expectedAllInsertionOrder, `is`(false))
    }
}
