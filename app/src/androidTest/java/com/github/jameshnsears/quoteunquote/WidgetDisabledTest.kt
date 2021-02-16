package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDisabledTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDisabled() {
        setupDatabases()

        val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
        every { quoteUnquoteWidget.getQuoteUnquoteModelInstance(any()) } returns quoteUnquoteModelDouble

        quoteUnquoteWidget.onEnabled(context)
        val contentPreferences = ContentPreferences(context)
        assertTrue(contentPreferences.contentFavouritesLocalCode.length == 10)
        quoteUnquoteWidget.onDisabled(context)

        assertEmptyDatabases()
        assertEmptySharedPreferences(contentPreferences)
    }

    private fun assertEmptySharedPreferences(contentPreferences: ContentPreferences) {
        assertTrue(contentPreferences.contentFavouritesLocalCode.isEmpty())
    }

    private fun assertEmptyDatabases() {
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID) == 0)
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
        assertTrue(quoteUnquoteModelDouble.countReported() == 0)
    }

    private fun setupDatabases() {
        insertQuotationsTestData01()

        setDefaultQuotationAsPreviousAll(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        setDefaultQuotationAsPreviousAuthor(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        setDefaultQuotationAsPreviousSearch(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID) == 3)

        markDefaultQuotationAsFavourite()
        markDefaultQuotationAsReported()
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }
}
