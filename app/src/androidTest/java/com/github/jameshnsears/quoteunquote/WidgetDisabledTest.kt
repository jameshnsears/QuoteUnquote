package com.github.jameshnsears.quoteunquote

import android.os.Build
import com.github.jameshnsears.quoteunquote.configure.fragment.archive.ArchivePreferences
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setupDatabase()

            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every { quoteUnquoteWidget.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

            quoteUnquoteWidget.onEnabled(context)
            val contentPreferences =
                ArchivePreferences(
                    0,
                    context
                )
            assertTrue(contentPreferences.transferLocalCode.length == 10)
            quoteUnquoteWidget.onDisabled(context)

            assertSharedPreferences(contentPreferences)
        }
    }

    private fun assertSharedPreferences(quotationsPreferences: ArchivePreferences) {
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
