package com.github.jameshnsears.quoteunquote.configure.fragment.content

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentViewModelTest : DatabaseTestHelper() {
    var contentViewModelDouble = ContentViewModelDouble()

    @Test
    fun countQuotations() {
        insertTestDataSet01()

        assertEquals("", 2, contentViewModelDouble.countAll().blockingGet().toInt())

        insertTestDataSet02()

        assertEquals("", 5, contentViewModelDouble.countAll().blockingGet().toInt())
    }

    @Test
    fun countAuthors() {
        insertTestDataSet01()
        insertTestDataSet02()
        insertTestDataSet03()

        assertEquals("", 5, contentViewModelDouble.authors().blockingGet().size)
    }

    @Test
    fun authorsSorted() {
        insertTestDataSet01()
        insertTestDataSet02()
        insertTestDataSet03()

        assertEquals(
            "",
            "a0",
            contentViewModelDouble.authorsSorted(contentViewModelDouble.authors().blockingGet())[0]
        )
        assertEquals("", 0, contentViewModelDouble.authorsIndex("a0"))

        assertEquals(
            "",
            "a5",
            contentViewModelDouble.authorsSorted(contentViewModelDouble.authors().blockingGet())[4]
        )
        assertEquals("", 4, contentViewModelDouble.authorsIndex("a5"))
    }

    @Test
    fun countAuthorQuotations() {
        insertTestDataSet01()
        insertTestDataSet02()
        insertTestDataSet03()

        contentViewModelDouble.authorPOJOList = contentViewModelDouble.authors().blockingGet()
        assertEquals("", 3, contentViewModelDouble.countAuthorQuotations("a2"))
    }

    @Test
    fun textSearchResults() {
        assertEquals("", 0, contentViewModelDouble.countQuotationWithText("q1").toInt())

        insertTestDataSet01()
        insertTestDataSet02()
        insertTestDataSet03()

        assertEquals("", 4, contentViewModelDouble.countQuotationWithText("q1").toInt())
    }

    @Test
    fun countFavourites() {
        insertTestDataSet01()

        assertEquals("", 0, contentViewModelDouble.countFavourites().blockingGet().toInt())

        assertEquals("", "{\"code\":\"bc5yX41a20\",\"digests\":[]}", contentViewModelDouble.favouritesToSend)
    }
}
