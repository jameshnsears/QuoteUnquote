package com.github.jameshnsears.quoteunquote.configure.fragment.content

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ContentViewModelTest : DatabaseTestHelper() {
    lateinit var contentViewModelDouble : ContentViewModelDouble

    @Before
    fun setUpDatabases() {
        contentViewModelDouble = ContentViewModelDouble(databaseRepositoryDouble)
    }

    @After
    fun shutdown() {
        contentViewModelDouble.shutdown()
    }

    @Test
    fun countAll() {
        insertTestDataSet01()
        assertEquals(
                "",
                2,
                contentViewModelDouble.countAll().blockingGet().toInt())

        insertTestDataSet02()
        assertEquals(
                "",
                5,
                contentViewModelDouble.countAll().blockingGet().toInt())
    }

//    @Test
//    fun authors() {
//        insertTestDataSet01()
//        insertTestDataSet02()
//        insertTestDataSet03()
//        Assert.assertEquals(
//                "",
//                5,
//                contentViewModel.authors().blockingGet().size)
//    }
//
//    @Test
//    fun authorsSorted() {
//        insertTestDataSet01()
//        insertTestDataSet02()
//        insertTestDataSet03()
//        Assert.assertEquals(
//                "",
//                "a0",
//                contentViewModel.authorsSorted(contentViewModel.authors().blockingGet())[0])
//        Assert.assertEquals(
//                "",
//                "a5",
//                contentViewModel.authorsSorted(contentViewModel.authors().blockingGet())[4])
//    }
//
//    @Test
//    fun countAuthorQuotations() {
//        insertTestDataSet01()
//        insertTestDataSet02()
//        insertTestDataSet03()
//        contentViewModel.authorPOJOList = contentViewModel.authors().blockingGet()
//        Assert.assertEquals(
//                "",
//                3,
//                contentViewModel.countAuthorQuotations("a2"))
//    }
//
//    @Test
//    fun countQuotationWithText() {
//        Assert.assertEquals("", 0, contentViewModel.countQuotationWithText("q1").toInt())
//        insertTestDataSet01()
//        insertTestDataSet02()
//        insertTestDataSet03()
//        Assert.assertEquals("", 4, contentViewModel.countQuotationWithText("q1").toInt())
//    }
//
//    @Test
//    fun countFavouriteQuotations() {
//        Assert.assertEquals("", 0, contentViewModel.countFavourites().blockingGet().toInt())
//    }
//
//    @Test
//    @Throws(NoNextQuotationAvailableException::class)
//    fun savePayload() {
//        insertTestDataSet01()
//        insertTestDataSet02()
//
//        // 1624c314
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false)
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest)
//
//        // d1
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false)
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest)
//        Assert.assertTrue("", contentViewModel.favouritesToSend.contains("\"digests\":[\"d1\",\"1624c314\"]"))
//    }

    /*
    @Test
    fun `todo - authorsSorted`() {
        fail("todo")
    }

    @Test
    fun `todo - countAuthorQuotations`() {
        fail("todo")
    }

    @Test
    fun `todo - getFavouritesToSend`() {
        fail("todo")
    }
     */
}
