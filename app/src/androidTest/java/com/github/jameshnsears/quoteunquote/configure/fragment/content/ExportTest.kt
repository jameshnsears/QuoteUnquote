package com.github.jameshnsears.quoteunquote.configure.fragment.content

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportTest : QuoteUnquoteModelUtility() {
    @Test
    fun exportFavourites() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 0)

        databaseRepositoryDouble.markAsFavourite(DatabaseRepository.DEFAULT_QUOTATION_DIGEST)

        // a digest that doesn't exist - i.e. quotation db migration deleted it
        databaseRepositoryDouble.markAsFavourite("xx")

        databaseRepositoryDouble.markAsFavourite("d2")
        databaseRepositoryDouble.markAsFavourite("d4")

        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 4)

        val exportedFavourites = quoteUnquoteModelDouble.exportFavourites()

        assertEquals(3, exportedFavourites?.size)
    }
}
