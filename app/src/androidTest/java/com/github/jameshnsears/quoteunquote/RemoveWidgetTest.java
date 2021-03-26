package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.utils.ContentType;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class RemoveWidgetTest extends DatabaseTestHelper {
    @Test
    public void twoWidgetsRemoveOneOfThem() {

        insertTestDataSet01();
        insertTestDataSet02();

        quoteUnquoteModel.setDefaultQuotation(1);

        quoteUnquoteModel.toggleFavourite(
                1, quoteUnquoteModel.getNext(1, ContentType.ALL).digest);

        quoteUnquoteModel.markAsReported(1);

        quoteUnquoteModel.setDefaultQuotation(2);

        final QuoteUnquoteModelFake quoteUnquoteModelSpy = spy(quoteUnquoteModel);
        doReturn(false).when(quoteUnquoteModelSpy).isRadioButtonFavouriteSelected(eq(2));
        quoteUnquoteModelSpy.toggleFavourite(
                2, quoteUnquoteModel.getNext(1, ContentType.ALL).digest);

        quoteUnquoteModel.markAsReported(2);

        assertEquals(
                "",
                1,
                quoteUnquoteModel.countPrevious(1, ContentType.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModel.countPrevious(2, ContentType.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                1,
                quoteUnquoteModel.countReported().intValue());

        quoteUnquoteModelSpy.toggleFavourite(
                2, quoteUnquoteModel.getNext(1, ContentType.ALL).digest);
        assertEquals(
                "",
                1,
                quoteUnquoteModel.countFavourites().blockingGet().intValue());

        ////////////////////////////////////////////

        quoteUnquoteModel.removeDatabaseEntriesForInstance(1);

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countPrevious(1, ContentType.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModel.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                1,
                quoteUnquoteModel.countReported().intValue());

        ////////////////////////////////////////////

        quoteUnquoteModel.removeDatabaseEntriesForAllInstances();

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countPrevious(2, ContentType.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countFavourites().blockingGet().intValue());

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countReported().intValue());
    }
}
