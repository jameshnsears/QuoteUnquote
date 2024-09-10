package com.github.jameshnsears.quoteunquote

import android.content.Context
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.logging.StdioTree
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class QuoteUnquoteWidgetTest {
    private val timberTree = StdioTree()

    private val widgetId = 1
    private val mockContext = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        if (Timber.treeCount == 0) {
            Timber.plant(timberTree)
        }
    }

    @After
    fun tearDown() {
        Timber.uproot(timberTree)
    }

    @Test
    fun `empty stub for testing`() {
        val quoteUnquoteWidgetSpy = spyk(QuoteUnquoteWidget())
        every {
            quoteUnquoteWidgetSpy.getQuotationsPreferences(
                mockContext,
                widgetId,
            )
        } returns
            getQuotationsPreferences(
                widgetId,
                mockContext,
                false,
                true,
            )
    }

    private fun getQuotationsPreferences(
        widgetId: Int,
        mockContext: Context,
        databaseInternal: Boolean,
        databaseExternal: Boolean,
    ): QuotationsPreferences {
        val quotationsPreferencesDouble = spyk(QuotationsPreferences(widgetId, mockContext))
        every { quotationsPreferencesDouble.databaseInternal } returns databaseInternal
        every { quotationsPreferencesDouble.databaseExternalCsv } returns databaseExternal

        return quotationsPreferencesDouble
    }
}
