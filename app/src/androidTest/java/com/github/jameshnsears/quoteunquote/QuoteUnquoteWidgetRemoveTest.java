package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QuoteUnquoteWidgetRemoveTest extends DatabaseTestHelper {
    @Test
    public void a() {
    }
//    @Test
//    public void twoWidgetsRemoveOneOfThem() {
//        insertTestDataSet01();
//        insertTestDataSet02();
//
//        quoteUnquoteModelDouble.setDefaultQuotation(1);
//
//        quoteUnquoteModelDouble.toggleFavourite(
//                1, quoteUnquoteModelDouble.getNext(1, ContentSelection.ALL).digest);
//
//        quoteUnquoteModelDouble.markAsReported(1);
//
//        quoteUnquoteModelDouble.setDefaultQuotation(2);
//
//        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = spy(quoteUnquoteModelDouble);
//        doReturn(false).when(quoteUnquoteModelSpy).isRadioButtonFavouriteSelected(eq(2));
//        quoteUnquoteModelSpy.toggleFavourite(
//                2, quoteUnquoteModelDouble.getNext(1, ContentSelection.ALL).digest);
//
//        quoteUnquoteModelDouble.markAsReported(2);
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countPrevious(1, ContentSelection.ALL));
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countPrevious(2, ContentSelection.ALL));
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countReported().intValue());
//
//        quoteUnquoteModelSpy.toggleFavourite(
//                2, quoteUnquoteModelDouble.getNext(1, ContentSelection.ALL).digest);
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());
//
//        ////////////////////////////////////////////
//
//        quoteUnquoteModelDouble.removeDatabaseEntriesForInstance(1);
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countPrevious(1, ContentSelection.ALL));
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());
//
//        assertEquals(
//                "",
//                1,
//                quoteUnquoteModelDouble.countReported().intValue());
//
//        ////////////////////////////////////////////
//
//        quoteUnquoteModelDouble.removeDatabaseEntriesForAllInstances();
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countPrevious(2, ContentSelection.ALL));
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());
//
//        assertEquals(
//                "",
//                0,
//                quoteUnquoteModelDouble.countReported().intValue());
//    }
}
