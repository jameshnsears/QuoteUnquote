package com.github.jameshnsears.quoteunquote.listview;

import android.appwidget.AppWidgetManager;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith(AndroidJUnit4.class)
public class ListViewProviderTest extends QuoteUnquoteModelUtility {
    @Test
    public void countItemsInList() {
        insertQuotationsTestData01();

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdHelper.INSTANCE_01_WIDGET_ID);

        final ListViewProvider listViewProviderTestSpy = spy(new ListViewProvider(context, intent));
        doReturn(quoteUnquoteModelDouble).when(listViewProviderTestSpy).getQuoteUnquoteModel(context);

        final ContentPreferences preferenceAppearanceSpy = spy(new ContentPreferences(-1, context));
        doReturn(ContentSelection.ALL).when(preferenceAppearanceSpy).getContentSelection();
        listViewProviderTestSpy.contentPreferences = preferenceAppearanceSpy;

        listViewProviderTestSpy.onCreate();
        assertEquals("", 0, listViewProviderTestSpy.getCount());

        listViewProviderTestSpy.onDataSetChanged();
        assertEquals("", 1, listViewProviderTestSpy.getCount());
    }
}
