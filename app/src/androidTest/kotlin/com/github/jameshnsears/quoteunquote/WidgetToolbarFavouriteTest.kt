package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class WidgetToolbarFavouriteTest : QuoteUnquoteModelUtility() {
    @Test
    fun makeFavourite() {
        insertQuotationTestData01(true)

        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), equalTo(0))
        assertThat(databaseRepositoryDouble.isFavourite(true, getDefaultQuotation(true).digest), `is`(false))

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01,
            DatabaseRepository.getDefaultQuotationDigest(true),
        )

        assertThat(databaseRepositoryDouble.isFavourite(true, getDefaultQuotation(true).digest), `is`(true))
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), equalTo(1))

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01,
            DatabaseRepository.getDefaultQuotationDigest(true),
        )
    }
}
