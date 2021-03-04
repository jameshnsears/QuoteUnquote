package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

open class WidgetDisabledTest : QuoteUnquoteModelUtility() {
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
        assertEquals(0, PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01))
        assertTrue(contentPreferences.contentFavouritesLocalCode.isEmpty())
    }

    private fun assertEmptyDatabases() {
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01) == 0)
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 0)
        assertTrue(quoteUnquoteModelDouble.countReported() == 0)
    }

    private fun setupDatabases() {
        insertQuotationTestData01()

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_01)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01) == 3)

        markDefaultQuotationAsFavourite()
        markDefaultQuotationAsReported()
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }
}
