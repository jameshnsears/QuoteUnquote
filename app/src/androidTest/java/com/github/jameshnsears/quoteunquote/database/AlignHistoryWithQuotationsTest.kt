package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.junit.Assert.assertEquals
import org.junit.Test

class AlignHistoryWithQuotationsTest : QuoteUnquoteModelUtility() {
    @Test
    fun alignHistoryWithQuotationsTest() {
        // arrange - setup
        insertInternalQuotations()

        val firstDigest = "d1234567"

        databaseRepositoryDouble.markAsPrevious(
            1,
            ContentSelection.ALL,
            firstDigest
        )

        val secondDigest = "d2345678"

        databaseRepositoryDouble.markAsCurrent(
            1,
            secondDigest
        )

        databaseRepositoryDouble.markAsPrevious(
            1,
            ContentSelection.ALL,
            secondDigest
        )

        databaseRepositoryDouble.markAsFavourite(secondDigest)

        assertEquals(2, databaseRepositoryDouble.countPreviousCriteria(1))

        // act - execute code under test

        databaseRepositoryDouble.eraseQuotation(secondDigest)
        databaseRepositoryDouble.alignHistoryWithQuotations(1, context)

        // assert - test result

        assertEquals(0, databaseRepositoryDouble.countFavourites().blockingGet())

        assertEquals(1, databaseRepositoryDouble.countPreviousCriteria(1))

        assertEquals(firstDigest, databaseRepositoryDouble.getCurrentQuotation(1).digest)
    }
}
