package com.github.jameshnsears.quoteunquote

import android.appwidget.AppWidgetManager
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class WidgetToolbarFirstTest : QuoteUnquoteModelUtility() {
    private val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
    private val appWidgetManager = mockk<AppWidgetManager>()

    fun setup() {
        insertQuotationTestData01(true)

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
                true,
                WidgetIdHelper.WIDGET_ID_01,
                getDefaultQuotation(true).digest,
            )

            val quotationsPreferences =
                QuotationsPreferences(
                    WidgetIdHelper.WIDGET_ID_01,
                    context,
                )
            quotationsPreferences.contentSelection = ContentSelection.ALL

            onReceiveToolbarPressedFirst(quoteUnquoteWidget, appWidgetManager)
            assertThat(
                databaseRepositoryDouble.getCurrentQuotation(true, WidgetIdHelper.WIDGET_ID_01).digest,
                equalTo(getDefaultQuotation(true).digest),
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

            databaseRepositoryDouble.markAsFavourite(true, getDefaultQuotation(true).digest)
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
            assertThat(
                databaseRepositoryDouble.getCurrentQuotation(true, WidgetIdHelper.WIDGET_ID_01).digest,
                equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
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
            assertThat(
                databaseRepositoryDouble.getCurrentQuotation(true, WidgetIdHelper.WIDGET_ID_01).digest,
                equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
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
