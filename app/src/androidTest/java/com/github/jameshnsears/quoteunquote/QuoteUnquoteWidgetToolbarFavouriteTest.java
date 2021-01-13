package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QuoteUnquoteWidgetToolbarFavouriteTest extends DatabaseTestHelper {
    @Test
    public void a() {
    }
//    @Test
//    public void isNextQuotationFavourite() {
//        insertTestDataSet01();
//
//        setDefaultQuotation();
//
//        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = spy(quoteUnquoteModelDouble);
//        doReturn(false).when(quoteUnquoteModelSpy).isRadioButtonFavouriteSelected(ArgumentMatchers.eq(WidgetIdTestHelper.WIDGET_ID));
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelSpy.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES));
//
//        assertEquals(
//                "",
//                Integer.valueOf(0),
//                quoteUnquoteModelSpy.countFavourites().blockingGet());
//
//        final QuotationEntity nextQuotation = quoteUnquoteModelSpy.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL);
//
//        assertFalse("", quoteUnquoteModelSpy.isFavourite(WidgetIdTestHelper.WIDGET_ID, nextQuotation.digest));
//
//        // make a favourite
//        quoteUnquoteModelSpy.toggleFavourite(WidgetIdTestHelper.WIDGET_ID, nextQuotation.digest);
//
//        assertEquals(
//                "",
//                Integer.valueOf(1),
//                quoteUnquoteModelSpy.countFavourites().blockingGet());
//
//        assertTrue(
//                "",
//                quoteUnquoteModelSpy.isFavourite(WidgetIdTestHelper.WIDGET_ID,
//                        quoteUnquoteModelSpy.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest));
//
//        // remove the favourite, but mark as a previous
//        quoteUnquoteModelSpy.toggleFavourite(WidgetIdTestHelper.WIDGET_ID, nextQuotation.digest);
//
//        assertEquals(
//                "",
//                Integer.valueOf(0),
//                quoteUnquoteModelSpy.countFavourites().blockingGet());
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelSpy.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.FAVOURITES));
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelSpy.countPrevious(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL));
//    }
//
//    @Test
//    public void makeSomeFavouritesFromContentAll() throws NoNextQuotationAvailableException {
//        insertTestDataSet01();
//        insertTestDataSet02();
//
//        final List<String> expectedDigestsList = new ArrayList<>();
//
//        // make a favourite
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        assertEquals(
//                "",
//                Integer.valueOf(1),
//                quoteUnquoteModelDouble.countFavourites().blockingGet());
//
//        // make a favourite
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        // don't make a favourite
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//
//        // make a favourite
//        quoteUnquoteModelDouble.setNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL, false);
//        quoteUnquoteModelDouble.toggleFavourite(
//                WidgetIdTestHelper.WIDGET_ID,
//                quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdTestHelper.WIDGET_ID, ContentSelection.ALL).digest);
//
//        assertEquals(
//                "",
//                Integer.valueOf(3),
//                quoteUnquoteModelDouble.countFavourites().blockingGet());
//
//        // the database returns in prior order
//        Collections.reverse(expectedDigestsList);
//
//        assertEquals(
//                "",
//                expectedDigestsList,
//                quoteUnquoteModelDouble.getFavourites());
//    }
}
