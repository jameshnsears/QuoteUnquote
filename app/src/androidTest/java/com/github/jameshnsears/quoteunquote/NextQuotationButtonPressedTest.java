package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.utils.ContentType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class NextQuotationButtonPressedTest extends DatabaseTestHelper {
    @Test
    public void missingQuotationDigestProvidedByFavouritesReceive() {
        assertNull("", quoteUnquoteModel.getDatabaseRepositoryFake().getQuotation("blah"));
    }

    @Test
    public void contentTypeAll() throws NoNextQuotationAvailableException {
        insertTestDataSet01();

        setDefaultQuotation();

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);

        assertEquals(
                "check that we're not using on DEFAULT_QUOTATION_DIGEST",
                "d1",
                quoteUnquoteModel.getNext(
                        widgetID, ContentType.ALL).digest);

        assertEquals(
                "make sure history contains correct ContentType",
                2,
                quoteUnquoteModel
                        .countPrevious(widgetID, ContentType.ALL));
    }

    @Test(expected = NoNextQuotationAvailableException.class)
    public void noMoreQuotations() throws NoNextQuotationAvailableException {
        insertTestDataSet01();

        setDefaultQuotation();

        try {
            quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        } catch (NoNextQuotationAvailableException e) {
            fail(e.getMessage());
        }


        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        fail("");
    }

    @Test
    public void contentTypeFavourites() throws NoNextQuotationAvailableException {
        insertTestDataSet01();
        insertTestDataSet02();

        quoteUnquoteModel.removeDatabaseEntriesForInstance(widgetID);

        final List<String> expectedFavouritesDigestList = new ArrayList<>();

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(1), quoteUnquoteModel.countFavourites().blockingGet());
        assertEquals("", 0, quoteUnquoteModel.countPrevious(widgetID, ContentType.FAVOURITES));

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(2), quoteUnquoteModel.countFavourites().blockingGet());

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);

        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedFavouritesDigestList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertEquals(
                "confirm correct # of favourites",
                Integer.valueOf(3), quoteUnquoteModel.countFavourites().blockingGet());

        Collections.reverse(expectedFavouritesDigestList);

        assertEquals(
                "",
                expectedFavouritesDigestList,
                quoteUnquoteModel.getFavourites());

        // switch into ContentType.Favourites and move through them until we run out of favourite quotations
        quoteUnquoteModel.setNext(widgetID, ContentType.FAVOURITES);
        quoteUnquoteModel.setNext(widgetID, ContentType.FAVOURITES);
        quoteUnquoteModel.setNext(widgetID, ContentType.FAVOURITES);
        try {
            quoteUnquoteModel.setNext(widgetID, ContentType.FAVOURITES);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            assertSame("", ContentType.FAVOURITES, e.contentType);
        }

        // make sure previous quotations same as available favourites
        final List<String> previousQuotations
                = quoteUnquoteModel.getPrevious(widgetID, ContentType.FAVOURITES);
        Collections.sort(previousQuotations);

        final List<String> favouriteQuotations
                = quoteUnquoteModel.getFavourites();
        Collections.sort(favouriteQuotations);

        assertEquals("", favouriteQuotations, previousQuotations);
    }

    @Test
    public void contentTypeAuthor() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        setDefaultQuotation();

        final QuoteUnquoteModelFake quoteUnquoteModelSpy = spy(quoteUnquoteModel);
        doReturn("a2").when(quoteUnquoteModelSpy).getPreferencesAuthorSearch(eq(widgetID));


        // user chooses a2 as author and keeps pressing new quotation

        // each time user selects a new author then the prior history is deleted
        quoteUnquoteModelSpy.deletePrevious(widgetID, ContentType.AUTHOR);

        // the default quotation should still be in the history
        assertEquals(
                "",
                1,
                quoteUnquoteModel.countPrevious(widgetID, ContentType.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModel.countPrevious(widgetID, ContentType.AUTHOR));

        try {
            quoteUnquoteModelSpy.setNext(widgetID, ContentType.AUTHOR);
            assertEquals(
                    "",
                    1,
                    quoteUnquoteModel.countPrevious(widgetID, ContentType.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(widgetID, ContentType.AUTHOR);
            assertEquals(
                    "",
                    2,
                    quoteUnquoteModel.countPrevious(widgetID, ContentType.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(widgetID, ContentType.AUTHOR);
            assertEquals(
                    "",
                    3,
                    quoteUnquoteModel.countPrevious(widgetID, ContentType.AUTHOR));
        } catch (NoNextQuotationAvailableException e) {
            fail("");
        }

        try {
            quoteUnquoteModelSpy.setNext(widgetID, ContentType.AUTHOR);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            // user would see a Toast
            assertSame("", ContentType.AUTHOR, e.contentType);
        }

        assertEquals("", 3, quoteUnquoteModelSpy.countPreviousAuthor(widgetID));
    }

    @Test
    public void contentTypeQuotationText() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        // using "q1" as the keyword from test data

        try {
            quoteUnquoteModel.setNext(widgetID, ContentType.QUOTATION_TEXT);
            quoteUnquoteModel.setNext(widgetID, ContentType.QUOTATION_TEXT);
            quoteUnquoteModel.setNext(widgetID, ContentType.QUOTATION_TEXT);
            quoteUnquoteModel.setNext(widgetID, ContentType.QUOTATION_TEXT);

            quoteUnquoteModel.setNext(widgetID, ContentType.QUOTATION_TEXT);
            fail("");
        } catch (NoNextQuotationAvailableException e) {
            assertSame("", ContentType.QUOTATION_TEXT, e.contentType);
        }

        assertEquals(
                "",
                4,
                quoteUnquoteModel.countPrevious(widgetID, ContentType.QUOTATION_TEXT));
    }
}
