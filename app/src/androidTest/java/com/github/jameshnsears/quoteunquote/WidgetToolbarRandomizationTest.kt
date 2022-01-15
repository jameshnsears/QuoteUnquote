package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class WidgetToolbarRandomizationTest : QuoteUnquoteModelUtility() {
    val expectedAllSequentialOrder = mutableListOf(
        "7a36e553",
        "d1",
        "d2",
        "d3",
        "d4",
        "d5",
        "d6"
    )

    @Before
    fun setup() {
        if (Timber.treeCount == 0) {
            Timber.plant(MethodLineLoggingTree())
        }

        databaseRepositoryDouble.erase()
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
        assertInsertionOrder(expectedAllSequentialOrder)

        assertNextOrder(expectedAllSequentialOrder)

        // use the Next quotations
        for (i in 1..quoteUnquoteModelDouble.countAll().blockingGet()) {
            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)
        }

        var actualPreviousOrder = quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL
        )
        actualPreviousOrder?.reverse()

        assertTrue(expectedAllSequentialOrder == actualPreviousOrder)
    }

    private fun assertInsertionOrder(expectedAllQuotationsOrder: MutableList<String>) {
        var actualAllQuotationsOrder = mutableListOf<String>()
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

        var actualPreviousOrder = quoteUnquoteModelDouble.databaseRepository?.getPreviousDigests(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL
        )
        actualPreviousOrder?.reverse()

        assertFalse(expectedAllSequentialOrder == actualPreviousOrder)
    }

    /*
    "the randomize button stopped randomizing and instead behaves identical to the sequential button"

    Adam Smith @ 1/6 + Sequential button presses:
    1. Man
    2. Labour
    3. The
    4. It
    5. People
    6. There
     */
}
