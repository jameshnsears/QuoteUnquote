package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseSelectionTest : QuoteUnquoteModelUtility() {
    @Test
    fun insertInternal() {
        DatabaseRepository.useInternalDatabase = true

        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01,
            DatabaseRepository.getDefaultQuotationDigest()
        )

        assertEquals(
            7,
            databaseRepositoryDouble.countAll().blockingGet()
        )
        assertEquals(
            5,
            databaseRepositoryDouble.authorsAndQuotationCounts.blockingGet().size
        )
    }

    @Test
    fun insertExternal() {
        DatabaseRepository.useInternalDatabase = false

        insertQuotationTestData01()
        insertQuotationTestData02()

        assertEquals(
            5,
            databaseRepositoryDouble.countAll().blockingGet()
        )
        assertEquals(
            4,
            databaseRepositoryDouble.authorsAndQuotationCounts.blockingGet().size
        )
    }
}
