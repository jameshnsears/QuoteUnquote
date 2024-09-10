package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertFalse
import org.junit.Test

class WidgetDeletedTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDeleted() {
        if (canWorkWithMockk()) {
            // arrange
            setupDatabase()
            setupSharedPreferences()

            // act
            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every {
                quoteUnquoteWidget.getQuoteUnquoteModel(WidgetIdHelper.WIDGET_ID_01, any())
            } returns quoteUnquoteModelDouble

            quoteUnquoteWidget.onEnabled(context)
            val syncPreferences =
                SyncPreferences(
                    0,
                    context,
                )
            assertThat(syncPreferences.transferLocalCode.length, `is`(10))

            assertEquals("N/A", syncPreferences.lastSuccessfulCloudBackupTimestamp)
            assertFalse(syncPreferences.autoCloudBackup)

            quoteUnquoteWidget.onDeleted(context, intArrayOf(WidgetIdHelper.WIDGET_ID_01))

            // assert
            assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), `is`(0))
            assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02), `is`(3))
            assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))

            assertEquals(
                0,
                PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01),
            )
            assertEquals(
                1,
                PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_02),
            )
            assertFalse(syncPreferences.transferLocalCode.isEmpty())
        }
    }

    private fun setupDatabase() {
        insertQuotationTestData01()

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_01)
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), `is`(3))

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationAuthor(WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationSearch(WidgetIdHelper.WIDGET_ID_02)
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02), `is`(3))

        markDefaultQuotationAsFavourite()
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))

        // double check only 1 still a favourite
        markDefaultQuotationAsFavourite()
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))
    }

    private fun setupSharedPreferences() {
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_01, context).appearanceQuotationTextSize = 1
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_02, context).appearanceQuotationTextSize = 1
    }
}
