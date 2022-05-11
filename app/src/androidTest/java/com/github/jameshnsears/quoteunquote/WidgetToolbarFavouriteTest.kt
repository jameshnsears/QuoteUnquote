package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetToolbarFavouriteTest : QuoteUnquoteModelUtility() {
    @Test
    fun makeFavourite() {
        insertQuotationTestData01()

        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 0)
        assertFalse(databaseRepositoryDouble.isFavourite(getDefaultQuotation().digest))

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01, DatabaseRepository.getDefaultQuotationDigest()
        )

        assertTrue(databaseRepositoryDouble.isFavourite(getDefaultQuotation().digest))
        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 1)

        quoteUnquoteModelDouble.toggleFavourite(
            WidgetIdHelper.WIDGET_ID_01, DatabaseRepository.getDefaultQuotationDigest()
        )
    }
}
