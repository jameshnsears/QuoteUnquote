package com.github.jameshnsears.quoteunquote;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith(AndroidJUnit4.class)
public class WidgetDeletedTest extends QuoteUnquoteModelUtility {
    @Test
    public void twoWidgetsOneDeleted() {

        insertDataset01();
        insertDataset02();

        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.INSTANCE_01_WIDGET_ID);

        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);

        quoteUnquoteModelDouble.reportQuotation(WidgetIdHelper.INSTANCE_01_WIDGET_ID);

        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.INSTANCE_02_WIDGET_ID);

        final QuoteUnquoteModelDouble quoteUnquoteModelSpy = spy(quoteUnquoteModelDouble);
        doReturn(false).when(quoteUnquoteModelSpy).selectedContentTypeIsFavourite(eq(WidgetIdHelper.INSTANCE_02_WIDGET_ID));

        quoteUnquoteModelSpy.toggleFavourite(
                WidgetIdHelper.INSTANCE_02_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);

        quoteUnquoteModelDouble.reportQuotation(WidgetIdHelper.INSTANCE_02_WIDGET_ID);

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(
                        WidgetIdHelper.INSTANCE_02_WIDGET_ID, ContentSelection.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites());

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported());

        quoteUnquoteModelSpy.toggleFavourite(
                WidgetIdHelper.INSTANCE_02_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).digest);
        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites());

        ////////////////////////////////////////////

        quoteUnquoteModelDouble.delete(WidgetIdHelper.INSTANCE_01_WIDGET_ID);

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL));

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites());

        assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported());

        ////////////////////////////////////////////

        quoteUnquoteModelDouble.disable();

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_02_WIDGET_ID, ContentSelection.ALL));

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites());

        assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countReported());
    }
}
