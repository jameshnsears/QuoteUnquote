package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

open class WidgetDisabledTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDisabled() {
        if (canWorkWithMockk()) {
            setupDatabase()

            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every {
                quoteUnquoteWidget.getQuoteUnquoteModel(
                    WidgetIdHelper.WIDGET_ID_01,
                    any(),
                )
            } returns quoteUnquoteModelDouble

            quoteUnquoteWidget.onEnabled(context)
            val contentPreferences =
                SyncPreferences(
                    0,
                    context,
                )
            assertThat(contentPreferences.transferLocalCode.length, equalTo(10))
            quoteUnquoteWidget.onDisabled(context)

            assertSharedPreferences(contentPreferences)
        }
    }

    private fun assertSharedPreferences(quotationsPreferences: SyncPreferences) {
        assertThat(
            PreferencesFacade.countPreferences(context, WidgetIdHelper.WIDGET_ID_01),
            equalTo(0),
        )
        // key exist's we jave haven't defined a value
        assertThat(quotationsPreferences.transferLocalCode.isEmpty(), `is`(false))
    }

    private fun setupDatabase() {
        insertQuotationTestData01(true)

        setDefaultQuotationAll(true, WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationAuthor(true, WidgetIdHelper.WIDGET_ID_01)
        setDefaultQuotationSearch(true, WidgetIdHelper.WIDGET_ID_01)
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), equalTo(3))

        markDefaultQuotationAsFavourite(true)
        assertThat(quoteUnquoteModelDouble.countFavourites().blockingGet(), equalTo(1))
    }
}
