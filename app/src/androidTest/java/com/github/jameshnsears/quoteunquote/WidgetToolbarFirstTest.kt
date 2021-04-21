package com.github.jameshnsears.quoteunquote

import android.appwidget.AppWidgetManager
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetToolbarFirstTest : QuoteUnquoteModelUtility() {
    val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
    val appWidgetManager = mockk<AppWidgetManager>()

    fun setup() {
        insertQuotationTestData01()

        every { quoteUnquoteWidget.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

        every { appWidgetManager.notifyAppWidgetViewDataChanged(WidgetIdHelper.WIDGET_ID_01, any()) } returns Unit

        quoteUnquoteWidget.onEnabled(context)
    }

    @Test
    fun firstAll() {
        setup()

        databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, getDefaultQuotation().digest)

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.ALL

        onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
        assertEquals(
            getDefaultQuotation().digest,
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest
        )
    }

    @Test
    fun firstFavourite() {
        setup()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.FAVOURITES

        databaseRepositoryDouble.markAsFavourite(getDefaultQuotation().digest)
        onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
    }

    @Test
    fun firstAuthor() {
        setup()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthor = "a0"

        onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
        assertEquals(
            "1624c314",
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest
        )
    }

    @Test
    fun firstSearch() {
        setup()

        val contentPreferences = ContentPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        contentPreferences.contentSelection = ContentSelection.AUTHOR
        contentPreferences.contentSelectionAuthor = "a0"

        onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
        assertEquals(
            "1624c314",
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest
        )
    }

    private fun onReceiveToolbarPressedFirst(
        quoteUnquoteWidget: QuoteUnquoteWidget,
        appWidgetManager: AppWidgetManager
    ) {
        quoteUnquoteWidget.onReceiveToolbarPressedFirst(
            context,
            WidgetIdHelper.WIDGET_ID_01,
            appWidgetManager
        )
    }
}
