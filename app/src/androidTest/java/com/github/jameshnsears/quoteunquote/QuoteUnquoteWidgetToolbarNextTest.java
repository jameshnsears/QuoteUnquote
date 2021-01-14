package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;


@RunWith(AndroidJUnit4.class)
public class QuoteUnquoteWidgetToolbarNextTest extends DatabaseTestHelper {
    @Test
    public void missingQuotationDigestProvidedByFavouritesReceive() {
        assertNull("", quoteUnquoteModelDouble.databaseRepository.getQuotation("blah"));
    }

    @Test
    public void contentTypeAll() throws NoNextQuotationAvailableException {
        insertTestDataSet01();

        setDefaultQuotation();

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);

        assertEquals(
                "check that we're not using on DEFAULT_QUOTATION_DIGEST",
                "d1",
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "make sure history contains correct ContentType",
                2,
                quoteUnquoteModelDouble
                        .countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL));
    }

    @Test(expected = NoNextQuotationAvailableException.class)
    public void noMoreQuotations() throws NoNextQuotationAvailableException {
        insertTestDataSet01();

        setDefaultQuotation();

        try {
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);
        } catch (NoNextQuotationAvailableException e) {
            fail(e.getMessage());
        }


        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);
        fail("");
    }

    @Test
    public void contentTypeFavourites() throws NoNextQuotationAvailableException {
        insertTestDataSet01();
        insertTestDataSet02();

        quoteUnquoteModelDouble.removeDatabaseEntriesForInstance(WidgetIdHelper.WIDGET_ID);

        final List<String> expectedFavouritesDigestList = new ArrayList<>();

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(1), quoteUnquoteModelDouble.countFavourites().blockingGet());
        assertEquals("", 0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES));

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(2), quoteUnquoteModelDouble.countFavourites().blockingGet());

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);

        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(3), quoteUnquoteModelDouble.countFavourites().blockingGet());

        Collections.reverse(expectedFavouritesDigestList);

        assertEquals(
                "",
                expectedFavouritesDigestList,
                quoteUnquoteModelDouble.getFavourites());

        // switch into ContentType.Favourites and move through them until we run out of favourite quotations
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
        try {
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES, false);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            assertSame("", ContentSelection.FAVOURITES, e.contentSelection);
        }

        // make sure previous quotations same as available favourites
        final List<String> previousQuotations
                = quoteUnquoteModelDouble.getPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.FAVOURITES);
        Collections.sort(previousQuotations);

        final List<String> favouriteQuotations
                = quoteUnquoteModelDouble.getFavourites();
        Collections.sort(favouriteQuotations);

        assertEquals("", favouriteQuotations, previousQuotations);
    }

    @Test
    public void contentTypeAuthor() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        setDefaultQuotation();

        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = Mockito.spy(quoteUnquoteModelDouble);
        doReturn("a2").when(quoteUnquoteModelSpy).getPreferencesAuthorSearch(ArgumentMatchers.eq(WidgetIdHelper.WIDGET_ID));


        // user chooses a2 as author and keeps pressing new quotation

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelSpy.deletePrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR);

        // the default quotation should still be in the history
        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR));

        try {
            quoteUnquoteModelSpy.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
            assertEquals(
                    "",
                    1,
                    quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
            assertEquals(
                    "",
                    2,
                    quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
            assertEquals(
                    "",
                    3,
                    quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.AUTHOR, false);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            // user would see a Toast
            assertSame("", ContentSelection.AUTHOR, e.contentSelection);
        }

        assertEquals("", 3, quoteUnquoteModelSpy.countPreviousAuthor(WidgetIdHelper.WIDGET_ID));
    }

    @Test
    public void contentTypeQuotationText() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        // using "q1" as the keyword from test data

        try {
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH, false);
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH, false);
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH, false);
            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH, false);

            quoteUnquoteModelDouble.setNext(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH, false);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            assertSame("", ContentSelection.SEARCH, e.contentSelection);
        }

        assertEquals(
                "",
                4,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID, ContentSelection.SEARCH));
    }
}
