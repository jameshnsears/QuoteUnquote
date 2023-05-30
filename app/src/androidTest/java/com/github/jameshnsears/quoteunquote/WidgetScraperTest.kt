package com.github.jameshnsears.quoteunquote

import android.content.Context
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
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

        assertEquals(
            ImportHelper.DEFAULT_DIGEST,
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest,
        )
    }

    @Test
    fun new() {
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

        val secondQuotation = QuotationEntity(
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

        assertEquals(
            "1/1",
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01,
                QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context),
            ),
        )

        val thirdQuotation = QuotationEntity(
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

        assertEquals(
            "1/1",
            quoteUnquoteModelDouble.getCurrentPosition(
                WidgetIdHelper.WIDGET_ID_01,
                QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, context),
            ),
        )
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
        DatabaseRepository.useInternalDatabase = false

        databaseRepositoryDouble.insertQuotationExternal(
            QuotationEntity(
                ImportHelper.DEFAULT_DIGEST,
                "?",
                "a1",
                "q1",
            ),
        )

        assertEquals(1, databaseRepositoryDouble.countAll().blockingGet())

        databaseRepositoryDouble.markAsCurrent(
            WidgetIdHelper.WIDGET_ID_01,
            ImportHelper.DEFAULT_DIGEST,
        )
    }
}
