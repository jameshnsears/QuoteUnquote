package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith(AndroidJUnit4.class)
public class WidgetToolbarFavouriteTest extends QuoteUnquoteModelUtility {
    @Test
    public void isNextQuotationFavourite() {
        insertDataset01();

        setDefaultQuotation();

        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = spy(quoteUnquoteModelDouble);
        doReturn(false).when(quoteUnquoteModelSpy).selectedContentTypeIsFavourite(ArgumentMatchers.eq(WidgetIdHelper.INSTANCE_01_WIDGET_ID));

        assertEquals(
                "",
                0,
                quoteUnquoteModelSpy.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.FAVOURITES));

        assertEquals(
                "",
                Integer.valueOf(0),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        final QuotationEntity nextQuotation = quoteUnquoteModelSpy.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL);

        assertFalse("", quoteUnquoteModelSpy.isFavourite(WidgetIdHelper.INSTANCE_01_WIDGET_ID, nextQuotation.digest));

        // make a favourite
        quoteUnquoteModelSpy.toggleFavourite(WidgetIdHelper.INSTANCE_01_WIDGET_ID, nextQuotation.digest);

        assertEquals(
                "",
                Integer.valueOf(1),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        assertTrue(
                "",
                quoteUnquoteModelSpy.isFavourite(WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                        quoteUnquoteModelSpy.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest));

        // remove the favourite, but mark as a previous
        quoteUnquoteModelSpy.toggleFavourite(WidgetIdHelper.INSTANCE_01_WIDGET_ID, nextQuotation.digest);

        assertEquals(
                "",
                Integer.valueOf(0),
                quoteUnquoteModelSpy.countFavourites().blockingGet());

        assertEquals(
                "",
                0,
                quoteUnquoteModelSpy.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.FAVOURITES));

        assertEquals(
                "",
                1,
                quoteUnquoteModelSpy.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL));
    }

    @Test
    public void makeSomeFavouritesFromContentAll() throws NoNextQuotationAvailableException {
        insertDataset01();
        insertDataset02();

        final List<String> expectedDigestsList = new ArrayList<>();

        // make a favourite
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "",
                Integer.valueOf(1),
                quoteUnquoteModelDouble.countFavourites().blockingGet());

        // make a favourite
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);

        // don't make a favourite
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false);

        // make a favourite
        quoteUnquoteModelDouble.setNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL, false);
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);
        expectedDigestsList.add(quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);

        assertEquals(
                "",
                Integer.valueOf(3),
                quoteUnquoteModelDouble.countFavourites().blockingGet());

        // the database returns in prior order
        Collections.reverse(expectedDigestsList);

        assertEquals(
                "",
                expectedDigestsList,
                quoteUnquoteModelDouble.getFavourites());
    }
}
