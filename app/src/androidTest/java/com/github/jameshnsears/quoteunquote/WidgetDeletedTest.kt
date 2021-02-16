package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class WidgetDeletedTest : QuoteUnquoteModelUtility() {
    @Test
    fun twoWidgetInstancesDeleteOne() {
        setupDatabases()
        setupSharedPreferences()

        val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
        every { quoteUnquoteWidget.getQuoteUnquoteModelInstance(any()) } returns quoteUnquoteModelDouble

        quoteUnquoteWidget.onEnabled(context)
        val contentPreferences = ContentPreferences(context)
        assertTrue(contentPreferences.contentFavouritesLocalCode.length == 10)
        quoteUnquoteWidget.onDeleted(context, intArrayOf(WidgetIdHelper.INSTANCE_01_WIDGET_ID))

        assertEmptyDatabasesForTheDeletedInstance()
        assertSharedPreferencesStillGood(contentPreferences)
    }

    private fun assertSharedPreferencesStillGood(contentPreferences: ContentPreferences) {
        assertEquals(0, PreferencesFacade.countPreferences(context, WidgetIdHelper.INSTANCE_01_WIDGET_ID))
        assertEquals(1, PreferencesFacade.countPreferences(context, WidgetIdHelper.INSTANCE_02_WIDGET_ID))

        assertFalse(contentPreferences.contentFavouritesLocalCode.isEmpty())
    }

    private fun assertEmptyDatabasesForTheDeletedInstance() {
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID) == 0)

        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_02_WIDGET_ID) == 3)
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }
    
    private fun setupDatabases() {
        insertQuotationsTestData01()

        setDefaultQuotationAsPreviousAll(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        setDefaultQuotationAsPreviousAuthor(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        setDefaultQuotationAsPreviousSearch(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID) == 3)

        setDefaultQuotationAsPreviousAll(WidgetIdHelper.INSTANCE_02_WIDGET_ID)
        setDefaultQuotationAsPreviousAuthor(WidgetIdHelper.INSTANCE_02_WIDGET_ID)
        setDefaultQuotationAsPreviousSearch(WidgetIdHelper.INSTANCE_02_WIDGET_ID)
        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_02_WIDGET_ID) == 3)

        markDefaultQuotationAsFavourite()
        markDefaultQuotationAsReported()
        assertTrue(quoteUnquoteModelDouble.countFavourites() == 1)
        assertTrue(quoteUnquoteModelDouble.countReported() == 1)
    }

    private fun setupSharedPreferences() {
        AppearancePreferences(WidgetIdHelper.INSTANCE_01_WIDGET_ID, context).appearanceTextSize = 1
        AppearancePreferences(WidgetIdHelper.INSTANCE_02_WIDGET_ID, context).appearanceTextSize = 1
    }
}
