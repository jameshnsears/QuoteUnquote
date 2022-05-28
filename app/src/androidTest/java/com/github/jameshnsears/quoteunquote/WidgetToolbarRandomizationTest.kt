package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class WidgetToolbarRandomizationTest : QuoteUnquoteModelUtility() {
    val expectedAllInsertionOrder = mutableListOf(
        "7a36e553",
        "d1234567",
        "d2345678",
        "d3456789",
        "d4567890",
        "d5678901",
        "d6789012"
    )

    @Before
    fun setup() {
        if (Timber.treeCount == 0) {
            Timber.plant(MethodLineLoggingTree())
        }

        databaseRepositoryDouble.eraseEverything()
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()
        assertEquals(7, quoteUnquoteModelDouble.countAll().blockingGet())

        assertEquals(
            0,
            quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        )
    }

    @Test
    fun traverseContentSelectionSequentially() {
        assertInsertionOrder(expectedAllInsertionOrder)

        val expectedNextNextOrder = mutableListOf(
            "7a36e553",
            "d1234567",
            "d2345678",
            "d3456789",
            "d6789012",
            "d4567890",
            "d5678901"
        )

        assertNextOrder(expectedNextNextOrder)

        // use the Next quotations
        for (i in 1..quoteUnquoteModelDouble.countAll().blockingGet()) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        }

        val actualPreviousOrder = quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL
        )
        actualPreviousOrder?.reverse()

        assertTrue(expectedNextNextOrder == actualPreviousOrder)
    }

    private fun assertInsertionOrder(expectedAllQuotationsOrder: MutableList<String>) {
        val actualAllQuotationsOrder = mutableListOf<String>()
        for (quotationEntity in quoteUnquoteModelDouble.allQuotations) {
            actualAllQuotationsOrder.add(quotationEntity.digest)
        }
        assertTrue(expectedAllQuotationsOrder == actualAllQuotationsOrder)
    }

    private fun assertNextOrder(expectedAllQuotationsOrder: MutableList<String>) {
        assertTrue(expectedAllQuotationsOrder == databaseRepositoryDouble.nextAllDigests)
    }

    @Test
    fun traverseContentSelectionRandomly() {
        for (i in 1..quoteUnquoteModelDouble.countAll().blockingGet()) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, true)
        }

        val actualPreviousOrder = quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL
        )
        actualPreviousOrder?.reverse()

        assertFalse(expectedAllInsertionOrder == actualPreviousOrder)
    }
}
