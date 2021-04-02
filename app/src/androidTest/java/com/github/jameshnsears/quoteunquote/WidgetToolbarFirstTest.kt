package com.github.jameshnsears.quoteunquote

import android.appwidget.AppWidgetManager
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class WidgetToolbarFirstTest : QuoteUnquoteModelUtility() {
    @Test
    fun getNextQuotation() {
        insertQuotationTestData01()

        val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
        every { quoteUnquoteWidget.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        val appWidgetManager = mockk<AppWidgetManager>()
        every { appWidgetManager.notifyAppWidgetViewDataChanged(WidgetIdHelper.WIDGET_ID_01, any()) } returns Unit

        quoteUnquoteWidget.onEnabled(context)

        onReceiveToolbarPressedFirst(ContentSelection.AUTHOR, quoteUnquoteWidget, appWidgetManager)
        onReceiveToolbarPressedFirst(ContentSelection.FAVOURITES, quoteUnquoteWidget, appWidgetManager)
        onReceiveToolbarPressedFirst(ContentSelection.SEARCH, quoteUnquoteWidget, appWidgetManager)
        onReceiveToolbarPressedFirst(ContentSelection.ALL, quoteUnquoteWidget, appWidgetManager)
    }

    private fun onReceiveToolbarPressedFirst(
        contentSelection: ContentSelection,
        quoteUnquoteWidget: QuoteUnquoteWidget,
        appWidgetManager: AppWidgetManager
    ) {
        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = contentSelection

        quoteUnquoteWidget.onReceiveToolbarPressedFirst(
            context,
            WidgetIdHelper.WIDGET_ID_01,
            appWidgetManager
        )
    }
}
