package com.github.jameshnsears.quoteunquote;

import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class FavouriteButtonPressedTest extends DatabaseTestHelper {
    @Test
    public void isNextQuotationFavourite() {
        insertTestDataSet01();

        setDefaultQuotation();

        final QuoteUnquoteModelFake quoteUnquoteModelSpy = spy(quoteUnquoteModel);
        doReturn(false).when(quoteUnquoteModelSpy).isRadioButtonFavouriteSelected(eq(widgetID));

        assertEquals(
                "",
                0,
                quoteUnquoteModelSpy.countPrevious(widgetID, ContentType.FAVOURITES));

        assertEquals(
                "",
                Integer.valueOf(0),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        final QuotationEntity nextQuotation = quoteUnquoteModelSpy.getNext(widgetID, ContentType.ALL);

        assertFalse("", quoteUnquoteModelSpy.isFavourite(widgetID, nextQuotation.digest));

        // make a favourite
        quoteUnquoteModelSpy.toggleFavourite(widgetID, nextQuotation.digest);

        assertEquals(
                "",
                Integer.valueOf(1),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        assertTrue(
                "",
                quoteUnquoteModelSpy.isFavourite(widgetID,
                    quoteUnquoteModelSpy.getNext(widgetID, ContentType.ALL).digest));

        // remove the favourite, but mark as a previous
        quoteUnquoteModelSpy.toggleFavourite(widgetID, nextQuotation.digest);

        assertEquals(
                "",
                Integer.valueOf(0),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        assertEquals(
                "",
                0,
                quoteUnquoteModelSpy.countPrevious(widgetID, ContentType.FAVOURITES));

        assertEquals(
                "",
                1,
                quoteUnquoteModelSpy.countPrevious(widgetID, ContentType.ALL));
    }

    @Test
    public void makeSomeFavouritesFromContentAll() throws NoNextQuotationAvailableException {
        insertTestDataSet01();
        insertTestDataSet02();

        final List<String> expectedDigestsList = new ArrayList<>();

        // make a favourite
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertEquals(
                "",
                Integer.valueOf(1),
                quoteUnquoteModel.countFavourites().blockingGet());

        // make a favourite
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        // don't make a favourite
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);

        // make a favourite
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertEquals(
                "",
                Integer.valueOf(3),
                quoteUnquoteModel.countFavourites().blockingGet());

        // the database returns in prior order
        Collections.reverse(expectedDigestsList);

        assertEquals(
                "",
                expectedDigestsList,
                quoteUnquoteModel.getFavourites());
    }
}
