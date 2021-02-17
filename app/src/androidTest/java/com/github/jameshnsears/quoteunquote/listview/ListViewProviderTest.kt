package com.github.jameshnsears.quoteunquote.listview

import android.appwidget.AppWidgetManager
import android.content.Intent
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test

class ListViewProviderTest : QuoteUnquoteModelUtility() {
    @Test
    fun countItemsInList() {
        insertQuotationsTestData01()
        val intent = Intent()
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdHelper.INSTANCE_01_WIDGET_ID)

        val listViewProvider = spyk(ListViewProvider(context, intent))
        every { listViewProvider.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

        val contentPreferences = spyk(ContentPreferences(context))
        every { contentPreferences.contentSelection } returns ContentSelection.ALL

        every { listViewProvider.getContentPreferences(any()) } returns contentPreferences

        listViewProvider.onCreate()
        assertEquals(0, listViewProvider.count)

        listViewProvider.onDataSetChanged()

        assertEquals(1, listViewProvider.count)
    }
}
