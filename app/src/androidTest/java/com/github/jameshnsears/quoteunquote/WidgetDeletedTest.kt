package com.github.jameshnsears.quoteunquote

import android.os.Build
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.transfer.TransferPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDeletedTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDeleted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setupDatabase()
            setupSharedPreferences()

            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every { quoteUnquoteWidget.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

            quoteUnquoteWidget.onEnabled(context)
            val transferPreferences =
                TransferPreferences(
                    context
                )
            assertTrue(transferPreferences.transferLocalCode.length == 10)
            quoteUnquoteWidget.onDeleted(context, intArrayOf(WidgetIdHelper.WIDGET_ID_01))

            assertDatabase()
            assertSharedPreferences(transferPreferences)
        }
    }

    private fun assertSharedPreferences(transferPreferences: TransferPreferences) {
        assertEquals(0, PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01))
        assertEquals(1, PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_02))

        assertFalse(transferPreferences.transferLocalCode.isEmpty())
    }

    private fun assertDatabase() {
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01) == 0)

        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02) == 3)
        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 1)
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }

    private fun setupDatabase() {
        insertQuotationTestData01()

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_01)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01) == 3)

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_02)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02) == 3)

        markDefaultQuotationAsFavourite()
        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 1)

        // double check only 1 still a favourite
        markDefaultQuotationAsFavourite()
        assertTrue(quoteUnquoteModelDouble.countFavourites().blockingGet() == 1)

        markDefaultQuotationAsReported()
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)

        // double check only 1 still reported
        markDefaultQuotationAsReported()
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }

    private fun setupSharedPreferences() {
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_01, context).appearanceTextSize = 1
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_02, context).appearanceTextSize = 1
    }
}
