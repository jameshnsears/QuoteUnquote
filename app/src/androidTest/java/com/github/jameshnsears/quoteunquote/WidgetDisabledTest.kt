package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

open class WidgetDisabledTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDisabled() {
        if (canWorkWithMockk()) {
            setupDatabase()

            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every { quoteUnquoteWidget.getQuoteUnquoteModel(WidgetIdHelper.WIDGET_ID_01, any()) } returns quoteUnquoteModelDouble

            quoteUnquoteWidget.onEnabled(context)
            val contentPreferences =
                SyncPreferences(
                    0,
                    context
                )
            assertTrue(contentPreferences.transferLocalCode.length == 10)
            quoteUnquoteWidget.onDisabled(context)

            assertSharedPreferences(contentPreferences)
        }
    }

    private fun assertSharedPreferences(quotationsPreferences: SyncPreferences) {
        assertEquals(0, PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01))
        // key exist's we jave haven't defined a value
        assertFalse(quotationsPreferences.transferLocalCode.isEmpty())
    }

    private fun setupDatabase() {
        insertQuotationTestData01()

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_01)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01) == 3)

        markDefaultQuotationAsFavourite()
        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 1)
    }
}
