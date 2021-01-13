package com.github.jameshnsears.quoteunquote.listview;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ListViewProviderTest extends DatabaseTestHelper {
    @Test
    public void a() {
    }

//    @Test
//    public void listView() {
//        insertTestDataSet01();
//
//        final Intent intent = new Intent();
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdTestHelper.WIDGET_ID);
//
//        final Context mockContext = mock(Context.class);
//
//        final ListViewProvider listViewProviderTestSpy = spy(new ListViewProvider(mockContext, intent));
//        Mockito.doReturn(this.quoteUnquoteModelDouble).when(listViewProviderTestSpy).getQuoteUnquoteModel(mockContext);
//
//        final ContentPreferences preferenceAppearanceSpy = spy(new ContentPreferences(-1, mockContext));
//        doReturn(ContentSelection.ALL).when(preferenceAppearanceSpy).getConten tSelection();
//        listViewProviderTestSpy.contentPreferences = preferenceAppearanceSpy;
//
//        listViewProviderTestSpy.onCreate();
//        Assert.assertEquals("", 0, listViewProviderTestSpy.getCount());
//
//        listViewProviderTestSpy.onDataSetChanged();
//        Assert.assertEquals("", 1, listViewProviderTestSpy.getCount());
//    }
}
