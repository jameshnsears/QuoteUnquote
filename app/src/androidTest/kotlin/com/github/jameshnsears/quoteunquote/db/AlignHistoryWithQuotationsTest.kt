package com.github.jameshnsears.quoteunquote.db

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AlignHistoryWithQuotationsTest : QuoteUnquoteModelUtility() {
    @Test
    fun alignHistoryWithQuotationsTest() {
        // arrange - setup
        insertInternalQuotations()

        val firstDigest = "d1234567"

        databaseRepositoryDouble.markAsPrevious(
            true,
            1,
            ContentSelection.ALL,
            firstDigest,
        )

        val secondDigest = "d2345678"

        databaseRepositoryDouble.markAsCurrent(
            true,
            1,
            secondDigest,
        )

        databaseRepositoryDouble.markAsPrevious(
            true,
            1,
            ContentSelection.ALL,
            secondDigest,
        )

        databaseRepositoryDouble.markAsFavourite(true, secondDigest)

        assertThat(databaseRepositoryDouble.countPreviousCriteria(true, 1), equalTo(2))

        // act - execute code under test

        databaseRepositoryDouble.eraseQuotation(true, secondDigest)
        databaseRepositoryDouble.alignHistoryWithQuotations(true, 1, context)

        // assert - test result

        assertThat(databaseRepositoryDouble.countFavourites(true).blockingGet(), equalTo(0))

        assertThat(databaseRepositoryDouble.countPreviousCriteria(true, 1), equalTo(1))

        assertThat(databaseRepositoryDouble.getCurrentQuotation(true, 1).digest, equalTo(firstDigest))
    }
}
