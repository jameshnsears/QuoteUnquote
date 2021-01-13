package com.github.jameshnsears.quoteunquote.configure.fragment.content

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import org.junit.Test

class ContentViewModelTest : DatabaseTestHelper() {
    @Test
    fun a() {
    }

    /*
    //    @Before
//    fun setUpDatabases() {
//        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(AbstractDatabaseHistory.DATABASE_NAME)
//
//        // insert's against in memory database defined in QuoteUnquoteModelFake
//        quoteUnquoteModelDouble = QuoteUnquoteModelDouble()
//        contentViewModel = ContentViewModelDouble(quoteUnquoteModelDouble.databaseRepository)
//    }
//
//    @After
//    fun shutdown() {
//        contentViewModel.shutdown()
//    }
//
//    @Test
//    fun countAll() {
//        insertTestDataSet01()
//        Assert.assertEquals(
//                "",
//                2,
//                contentViewModel.countAll().blockingGet().toInt())
//        insertTestDataSet02()
//        Assert.assertEquals(
//                "",
//                5,
//                contentViewModel.countAll().blockingGet().toInt())
//    }
//
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
