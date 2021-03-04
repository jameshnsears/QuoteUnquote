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
import org.junit.Assert.assertNotNull
import org.junit.Test

class ListViewTest : QuoteUnquoteModelUtility() {
    @Test
    fun listViewService() {
        assertNotNull(ListViewService().onGetViewFactory(getIntent()) as ListViewProvider)
    }

    private fun getIntent(): Intent {
        val intent = Intent()
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdHelper.WIDGET_ID_01)
        return intent
    }

    @Test
    fun countItemsInList() {
        insertQuotationTestData01()

        val listViewProvider = spyk(ListViewProvider(context, getIntent()))
        every { listViewProvider.getQuoteUnquoteModel() } returns quoteUnquoteModelDouble

        val contentPreferences = ContentPreferences(context)
        contentPreferences.contentSelection = ContentSelection.ALL

        listViewProvider.onCreate()
        assertEquals(0, listViewProvider.count)

        listViewProvider.onDataSetChanged()

        assertEquals(1, listViewProvider.count)

        listViewProvider.onDestroy()
    }
}
