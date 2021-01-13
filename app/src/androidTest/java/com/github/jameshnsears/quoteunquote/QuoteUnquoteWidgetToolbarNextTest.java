package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class QuoteUnquoteWidgetToolbarNextTest extends DatabaseTestHelper {
    @Test
    public void a() {
    }
//    @Test
//    public void missingQuotationDigestProvidedByFavouritesReceive() {
//        assertNull("", quoteUnquoteModelDouble.databaseRepository.getQuotation("blah"));
//    }
//
//    @Test
//    public void contentTypeAll() throws NoNextQuotationAvailableException {
//        insertTestDataSet01();
//
//        setDefaultQuotation();
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//
//        Assert.assertEquals(
//                "check that we're not using on DEFAULT_QUOTATION_DIGEST",
//                "d1",
//                quoteUnquoteModelDouble.getNext(
//                        WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        Assert.assertEquals(
//                "make sure history contains correct ContentType",
//                2,
//                quoteUnquoteModelDouble
//                        .countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL));
//    }
//
//    @Test(expected = NoNextQuotationAvailableException.class)
//    public void noMoreQuotations() throws NoNextQuotationAvailableException {
//        insertTestDataSet01();
//
//        setDefaultQuotation();
//
//        try {
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        } catch (NoNextQuotationAvailableException e) {
//            fail(e.getMessage());
//        }
//
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        fail("");
//    }
//
//    @Test
//    public void contentTypeFavourites() throws NoNextQuotationAvailableException {
//        insertTestDataSet01();
//        insertTestDataSet02();
//
//        quoteUnquoteModelDouble.removeDatabaseEntriesForInstance(WidgetIdTestHelper.WIDGET_ID);
//
//        final List<String> expectedFavouritesDigestList = new ArrayList<>();
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        Assert.assertEquals(
//                "confirm correct # of favourites",
//                Integer.valueOf(1), quoteUnquoteModelDouble.countFavourites().blockingGet());
//        Assert.assertEquals("", 0, quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES));
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        Assert.assertEquals(
//                "confirm correct # of favourites",
//                Integer.valueOf(2), quoteUnquoteModelDouble.countFavourites().blockingGet());
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        Assert.assertEquals(
//                "confirm correct # of favourites",
//                Integer.valueOf(3), quoteUnquoteModelDouble.countFavourites().blockingGet());
//
//        Collections.reverse(expectedFavouritesDigestList);
//
//        Assert.assertEquals(
//                "",
//                expectedFavouritesDigestList,
//                quoteUnquoteModelDouble.getFavourites());
//
//        // switch into ContentType.Favourites and move through them until we run out of favourite quotations
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
//        try {
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
//            fail("");
//        } catch (NoNextQuotationAvailableException e) {
//            assertSame("", ContentSelection.FAVOURITES, e.contentSelection);
//        }
//
//        // make sure previous quotations same as available favourites
//        final List<String> previousQuotations
//                = quoteUnquoteModelDouble.getPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES);
//        Collections.sort(previousQuotations);
//
//        final List<String> favouriteQuotations
//                = quoteUnquoteModelDouble.getFavourites();
//        Collections.sort(favouriteQuotations);
//
//        assertEquals("", favouriteQuotations, previousQuotations);
//    }
//
//    @Test
//    public void contentTypeAuthor() {
//        insertTestDataSet01();
//        insertTestDataSet02();
//        insertTestDataSet03();
//
//        setDefaultQuotation();
//
//        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = Mockito.spy(quoteUnquoteModelDouble);
//        doReturn("a2").when(quoteUnquoteModelSpy).getPreferencesAuthorSearch(ArgumentMatchers.eq(WidgetIdTestHelper.WIDGET_ID));
//
//
//        // user chooses a2 as author and keeps pressing new quotation
//
//        // each time user selects a new author then the prior history is deleted
//        quoteUnquoteModelSpy.deletePrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR);
//
//        // the default quotation should still be in the history
//        Assert.assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL));
//
//        Assert.assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR));
//
//        try {
//            quoteUnquoteModelSpy.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
//            Assert.assertEquals(
//                    "",
//                    1,
//                    quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR));
//        } catch (NoNextQuotationAvailableException e) {
//            fail("");
//        }
//
//        try {
//            quoteUnquoteModelSpy.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
//            Assert.assertEquals(
//                    "",
//                    2,
//                    quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR));
//        } catch (NoNextQuotationAvailableException e) {
//            fail("");
//        }
//
//        try {
//            quoteUnquoteModelSpy.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
//            Assert.assertEquals(
//                    "",
//                    3,
//                    quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR));
//        } catch (NoNextQuotationAvailableException e) {
//            fail("");
//        }
//
//        try {
//            quoteUnquoteModelSpy.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
//            fail("");
//        } catch (NoNextQuotationAvailableException e) {
//            // user would see a Toast
//            assertSame("", ContentSelection.AUTHOR, e.contentSelection);
//        }
//
//        assertEquals("", 3, quoteUnquoteModelSpy.countPreviousAuthor(WidgetIdTestHelper.WIDGET_ID));
//    }
//
//    @Test
//    public void contentTypeQuotationText() {
//        insertTestDataSet01();
//        insertTestDataSet02();
//        insertTestDataSet03();
//
//        // using "q1" as the keyword from test data
//
//        try {
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH, false);
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH, false);
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH, false);
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH, false);
//
//            quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH, false);
//            fail("");
//        } catch (NoNextQuotationAvailableException e) {
//            assertSame("", ContentSelection.SEARCH, e.contentSelection);
//        }
//
//        Assert.assertEquals(
//                "",
//                4,
//                quoteUnquoteModelDouble.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.SEARCH));
//    }
}
