package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class QuoteUnquoteWidgetRemoveTest extends DatabaseTestHelper {
    @Test
    public void twoWidgetsRemoveOneOfThem() {
        final int widgetId01 = 1;
        final int widgetId02 = 2;

        insertTestDataSet01();
        insertTestDataSet02();

        quoteUnquoteModelDouble.setDefaultQuotation(widgetId01);

        quoteUnquoteModelDouble.toggleFavourite(
                widgetId01, quoteUnquoteModelDouble.getNext(widgetId01, ContentSelection.ALL).digest);

        quoteUnquoteModelDouble.markAsReported(widgetId01);

        quoteUnquoteModelDouble.setDefaultQuotation(widgetId02);

        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = spy(quoteUnquoteModelDouble);
        doReturn(false).when(quoteUnquoteModelSpy).isRadioButtonFavouriteSelected(eq(widgetId02));
        quoteUnquoteModelSpy.toggleFavourite(
                widgetId02, quoteUnquoteModelDouble.getNext(widgetId01, ContentSelection.ALL).digest);

        quoteUnquoteModelDouble.markAsReported(widgetId02);

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(widgetId01, ContentSelection.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(widgetId02, ContentSelection.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported().intValue());

        quoteUnquoteModelSpy.toggleFavourite(
                widgetId02, quoteUnquoteModelDouble.getNext(widgetId01, ContentSelection.ALL).digest);
        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());

        ////////////////////////////////////////////

        quoteUnquoteModelDouble.removeDatabaseEntriesForInstance(widgetId01);

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(widgetId01, ContentSelection.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported().intValue());

        ////////////////////////////////////////////

        quoteUnquoteModelDouble.removeDatabaseEntriesForAllInstances();

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(widgetId02, ContentSelection.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countReported().intValue());
    }
}
