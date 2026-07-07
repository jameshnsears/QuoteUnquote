package com.github.jameshnsears.quoteunquote.db

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DatabaseSelectionTest : QuoteUnquoteModelUtility() {
    @Test
    fun insertInternal() {
        quoteUnquoteModelDouble.setUseInternalDatabase(true)

        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
        insertQuotationTestData03(true)

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01,
            DatabaseRepository.getDefaultQuotationDigest(true),
        )

        assertThat(
            databaseRepositoryDouble.countAll(true).blockingGet(),
            equalTo(7),
        )
        assertThat(
            databaseRepositoryDouble.getAuthorsAndQuotationCounts(true, 1).blockingGet().size,
            equalTo(5),
        )
        assertThat(
            databaseRepositoryDouble.getAuthorsQuotationCount(true).blockingGet().size,
            equalTo(2),
        )
    }

    @Test
    fun insertExternal() {
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        insertQuotationTestData01(false)
        insertQuotationTestData02(false)

        assertThat(
            databaseRepositoryDouble.countAll(false).blockingGet(),
            equalTo(5),
        )
        assertThat(
            databaseRepositoryDouble.getAuthorsAndQuotationCounts(false, 1).blockingGet().size,
            equalTo(4),
        )
        assertThat(
            databaseRepositoryDouble.getAuthorsQuotationCount(false).blockingGet().size,
            equalTo(2),
        )
    }

    @Test
    fun databaseSelectionCoexistence() {
        insertInternalQuotations()
        insertExternalQuotations()

        assertThat(databaseRepositoryDouble.countAll(true).blockingGet(), equalTo(5))
        assertThat(databaseRepositoryDouble.countAll(false).blockingGet(), equalTo(2))
    }
}
