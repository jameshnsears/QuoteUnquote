package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteUnquoteModelTest : QuoteUnquoteModelUtility() {
    @Test
    fun countAll() {
        insertQuotationTestData01()

        assertEquals(
            2,
            quoteUnquoteModelDouble.countAll().blockingGet()
        )
    }

    @Test
    fun authorsSorted() {
        val sortedList =
            quoteUnquoteModelDouble.authorsSorted(
                mutableListOf(
                    AuthorPOJO("c"),
                    AuthorPOJO("b"),
                    AuthorPOJO("a")
                )
            )

        assertTrue(sortedList[0].equals("a"))
        assertTrue(sortedList[2].equals("c"))
    }

    @Test
    fun countAuthorQuotations() {
        val authorPOJO = AuthorPOJO("a2")
        authorPOJO.count = 2
        quoteUnquoteModelDouble.cachedAuthorPOJOList = mutableListOf(authorPOJO)

        assertEquals(2, quoteUnquoteModelDouble.countAuthorQuotations("a2"))
    }
}
