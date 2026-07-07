package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
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

            assertThat(syncPreferences.lastSuccessfulCloudBackupTimestamp, equalTo("N/A"))
            assertThat(syncPreferences.autoCloudBackup, `is`(false))

            quoteUnquoteWidget.onDeleted(context, intArrayOf(WidgetIdHelper.WIDGET_ID_01))

            // assert
            assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), `is`(0))
            assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02), `is`(3))
            assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))

            assertThat(
                PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01),
                `is`(0),
            )
            assertThat(
                PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_02),
                `is`(1),
            )
            assertThat(syncPreferences.transferLocalCode.isEmpty(), `is`(false))
        }
    }

    private fun setupDatabase() {
        insertQuotationTestData01(true)

        setDefaultQuotationAll(true, WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(true, WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(true, WidgetIdHelper.WIDGET_ID_01)
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), `is`(3))

        setDefaultQuotationAll(true, WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationAuthor(true, WidgetIdHelper.WIDGET_ID_02)
        setDefaultQuotationSearch(true, WidgetIdHelper.WIDGET_ID_02)
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_02), `is`(3))

        markDefaultQuotationAsFavourite(true)
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))

        // double check only 1 still a favourite
        markDefaultQuotationAsFavourite(true)
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), `is`(1))
    }

    private fun setupSharedPreferences() {
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_01, context).appearanceQuotationTextSize = 1
        AppearancePreferences(WidgetIdHelper.WIDGET_ID_02, context).appearanceQuotationTextSize = 1
    }
}
