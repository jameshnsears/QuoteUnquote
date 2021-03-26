package com.github.jameshnsears.quoteunquote.listview;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import com.github.jameshnsears.quoteunquote.DatabaseTestHelper;
import com.github.jameshnsears.quoteunquote.utils.ContentType;
import com.github.jameshnsears.quoteunquote.utils.Preferences;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class ListViewProviderTest extends DatabaseTestHelper {
    @Test
    public void listView() {
        insertTestDataSet01();

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        final Context mockContext = mock(Context.class);

        final ListViewProvider listViewProviderSpy = spy(new ListViewProvider(mockContext, intent));
        doReturn(this.quoteUnquoteModel).when(listViewProviderSpy).getQuoteUnquoteModel(mockContext);

        final Preferences preferencesSpy = spy(new Preferences(-1, mockContext));
        doReturn(ContentType.ALL).when(preferencesSpy).getSelectedContentType();
        listViewProviderSpy.preferences = preferencesSpy;

        listViewProviderSpy.onCreate();
        assertEquals("", 0, listViewProviderSpy.getCount());

        listViewProviderSpy.onDataSetChanged();
        assertEquals("", 1, listViewProviderSpy.getCount());
    }
}
