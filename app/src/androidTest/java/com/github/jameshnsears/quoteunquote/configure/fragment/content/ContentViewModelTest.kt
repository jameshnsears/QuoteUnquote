package com.github.jameshnsears.quoteunquote.configure.fragment.content

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentViewModelTest : DatabaseTestHelper() {
    var contentViewModelDouble = ContentViewModelDouble()

    @Test
    fun countQuotations() {
        insertQuotationTestData01()

        assertEquals("", 2, contentViewModelDouble.countAll().blockingGet().toInt())

        insertQuotationTestData02()

        assertEquals("", 5, contentViewModelDouble.countAll().blockingGet().toInt())
    }

    @Test
    fun countAuthors() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals("", 5, contentViewModelDouble.authors().blockingGet().size)
    }

    @Test
    fun checkAuthorsSpinnerSorted() {
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

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
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        contentViewModelDouble.authorPOJOList = contentViewModelDouble.authors().blockingGet()
        assertEquals("", 3, contentViewModelDouble.countAuthorQuotations("a2"))
    }

    @Test
    fun checkTextSearchResults() {
        assertEquals("", 0, contentViewModelDouble.countQuotationWithText("q1"))

        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        assertEquals("", 4, contentViewModelDouble.countQuotationWithText("q1"))
    }

    @Test
    fun countFavourites() {
        insertQuotationTestData01()

        assertEquals("", 0, contentViewModelDouble.countFavourites().blockingGet().toInt())

        var contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentFavouritesLocalCode = "bc5yX41a20"

        assertEquals(
            "",
            "{\"code\":\"bc5yX41a20\",\"digests\":[]}",
            contentViewModelDouble.getFavouritesToSend(context)
        )
    }
}
