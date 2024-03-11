package com.github.jameshnsears.quoteunquote

import android.appwidget.AppWidgetManager
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetToolbarFirstTest : QuoteUnquoteModelUtility() {
    private val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
    private val appWidgetManager = mockk<AppWidgetManager>()

    fun setup() {
        insertQuotationTestData01()

        every {
            quoteUnquoteWidget.getQuoteUnquoteModel(
                WidgetIdHelper.WIDGET_ID_01,
                any(),
            )
        } returns quoteUnquoteModelDouble

        every {
            appWidgetManager.notifyAppWidgetViewDataChanged(
                WidgetIdHelper.WIDGET_ID_01,
                any(),
            )
        } returns Unit

        quoteUnquoteWidget.onEnabled(context)
    }

    @Test
    fun firstAll() {
        if (canWorkWithMockk()) {
            setup()

            databaseRepositoryDouble.markAsCurrent(
                WidgetIdHelper.WIDGET_ID_01,
                getDefaultQuotation().digest,
            )

            val quotationsPreferences =
                QuotationsPreferences(
                    WidgetIdHelper.WIDGET_ID_01,
                    context,
                )
            quotationsPreferences.contentSelection = ContentSelection.ALL

            onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
            assertEquals(
                getDefaultQuotation().digest,
                databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest,
            )
        }
    }

    @Test
    fun firstFavourite() {
        if (canWorkWithMockk()) {
            setup()

            val quotationsPreferences =
                QuotationsPreferences(
                    WidgetIdHelper.WIDGET_ID_01,
                    context,
                )
            quotationsPreferences.contentSelection = ContentSelection.FAVOURITES

            databaseRepositoryDouble.markAsFavourite(getDefaultQuotation().digest)
            onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
        }
    }

    @Test
    fun firstAuthor() {
        if (canWorkWithMockk()) {
            setup()

            val quotationsPreferences =
                QuotationsPreferences(
                    WidgetIdHelper.WIDGET_ID_01,
                    context,
                )
            quotationsPreferences.contentSelection = ContentSelection.AUTHOR
            quotationsPreferences.contentSelectionAuthor = "a0"

            onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
            assertEquals(
                DatabaseRepository.getDefaultQuotationDigest(),
                databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest,
            )
        }
    }

    @Test
    fun firstSearch() {
        if (canWorkWithMockk()) {
            setup()

            val quotationsPreferences =
                QuotationsPreferences(
                    WidgetIdHelper.WIDGET_ID_01,
                    context,
                )
            quotationsPreferences.contentSelection = ContentSelection.AUTHOR
            quotationsPreferences.contentSelectionAuthor = "a0"

            onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
            assertEquals(
                DatabaseRepository.getDefaultQuotationDigest(),
                databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest,
            )
        }
    }

    private fun onReceiveToolbarPressedFirst(
        quoteUnquoteWidget: QuoteUnquoteWidget,
        appWidgetManager: AppWidgetManager,
    ) {
        quoteUnquoteWidget.onReceiveToolbarPressedFirst(
            context,
            WidgetIdHelper.WIDGET_ID_01,
            appWidgetManager,
        )
    }
}
