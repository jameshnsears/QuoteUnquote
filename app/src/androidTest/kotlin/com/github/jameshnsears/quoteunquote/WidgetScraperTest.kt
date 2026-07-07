package com.github.jameshnsears.quoteunquote

import android.content.Context
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class WidgetScraperTest : QuoteUnquoteModelUtility() {
    @Before
    fun setup() {
        insertDefaultQuotation()
    }

    // run in 29+ API only !

    @Test
    fun reuseDefault() {
        if (canWorkWithMockk()) {
            val quoteUnquoteWidget = spyk(QuoteUnquoteWidget())
            every {
                quoteUnquoteWidget.getQuotationsPreferences(
                    any(),
                    any(),
                )
            } returns
                getQuotationsPreferences(
                    context,
                    WidgetIdHelper.WIDGET_ID_01,
                    false,
                    true,
                )

            quoteUnquoteWidget.quoteUnquoteModel = quoteUnquoteModelDouble

            quoteUnquoteWidget.displayAppropriateScrapedQuotation(
                context,
                WidgetIdHelper.WIDGET_ID_01,
                "q1",
                "a1",
            )

            assertThat(
                databaseRepositoryDouble.getCurrentQuotation(false, WidgetIdHelper.WIDGET_ID_01).digest,
                equalTo(ImportHelper.DEFAULT_DIGEST),
            )
        }
    }

    @Test
    fun new() {
        if (canWorkWithMockk()) {
            val quoteUnquoteWidget = spyk(QuoteUnquoteWidget())
            every {
                quoteUnquoteWidget.getQuotationsPreferences(
                    any(),
                    any(),
                )
            } returns
                getQuotationsPreferences(
                    context,
                    WidgetIdHelper.WIDGET_ID_01,
                    false,
                    true,
                )

            quoteUnquoteWidget.quoteUnquoteModel = quoteUnquoteModelDouble
            quoteUnquoteModelDouble.setUseInternalDatabase(false)

            val secondQuotation =
                QuotationEntity(
                    ImportHelper.makeDigest("q2", "a2"),
                    "?",
                    "a2",
                    "q2",
                )
            quoteUnquoteWidget.displayAppropriateScrapedQuotation(
                context,
                WidgetIdHelper.WIDGET_ID_01,
                secondQuotation.quotation,
                secondQuotation.author,
            )

            assertThat(
                quoteUnquoteModelDouble.getCurrentPosition(
                    WidgetIdHelper.WIDGET_ID_01,
                    QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context),
                ),
                equalTo("1/1"),
            )

            val thirdQuotation =
                QuotationEntity(
                    ImportHelper.makeDigest("q2", "a2"),
                    "?",
                    "a1",
                    "q1",
                )
            quoteUnquoteWidget.displayAppropriateScrapedQuotation(
                context,
                WidgetIdHelper.WIDGET_ID_01,
                thirdQuotation.quotation,
                thirdQuotation.author,
            )

            assertThat(
                quoteUnquoteModelDouble.getCurrentPosition(
                    WidgetIdHelper.WIDGET_ID_01,
                    QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context),
                ),
                equalTo("1/1"),
            )
        }
    }

    private fun getQuotationsPreferences(
        mockContext: Context,
        widgetId: Int,
        databaseInternal: Boolean,
        databaseExternal: Boolean,
    ): QuotationsPreferences {
        val quotationsPreferencesDouble = spyk(QuotationsPreferences(widgetId, mockContext))
        every { quotationsPreferencesDouble.databaseInternal } returns databaseInternal
        every { quotationsPreferencesDouble.databaseExternalCsv } returns databaseExternal

        return quotationsPreferencesDouble
    }

    private fun insertDefaultQuotation() {
        databaseRepositoryDouble.insertQuotationExternal(
            QuotationEntity(
                ImportHelper.DEFAULT_DIGEST,
                "?",
                "a1",
                "q1",
            ),
        )

        assertThat(databaseRepositoryDouble.countAll(false).blockingGet(), equalTo(1))

        databaseRepositoryDouble.markAsCurrent(
            false,
            WidgetIdHelper.WIDGET_ID_01,
            ImportHelper.DEFAULT_DIGEST,
        )
    }
}
