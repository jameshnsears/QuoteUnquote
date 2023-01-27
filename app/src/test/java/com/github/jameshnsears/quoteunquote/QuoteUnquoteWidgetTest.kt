package com.github.jameshnsears.quoteunquote

import android.content.Context
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.logging.StdioTree
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Assert.assertTrue
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
    fun `cancel an already active externalObserver`() {
        launchExternalObserver(
            mockContext,
            widgetId,
            getQuotationsPreferences(
                widgetId,
                mockContext,
                false,
                true,
                "externalWatch/1/VerseOfTheDay.csv",
                true,
            ),
        )

        // now cancel the externalObserver
        Thread.sleep(QuoteUnquoteWidget.externalObserverInternal)
        QuoteUnquoteWidget.externalObserver.cancel(true)
        Thread.sleep(QuoteUnquoteWidget.externalObserverInternal * 2)

        assertTrue(QuoteUnquoteWidget.externalObserver.isCancelled)
    }

    private fun getQuotationsPreferences(
        widgetId: Int,
        mockContext: Context,
        databaseInternal: Boolean,
        databaseExternal: Boolean,
        databaseExternalPath: String,
        databaseExternalWatch: Boolean,
    ): QuotationsPreferences {
        val quotationsPreferencesDouble = spyk(QuotationsPreferences(widgetId, mockContext))
        every { quotationsPreferencesDouble.databaseInternal } returns databaseInternal
        every { quotationsPreferencesDouble.databaseExternal } returns databaseExternal
        every { quotationsPreferencesDouble.databaseExternalPath } returns javaClass.classLoader.getResource(
            databaseExternalPath,
        ).getPath()
        every { quotationsPreferencesDouble.databaseExternalWatch } returns databaseExternalWatch

        return quotationsPreferencesDouble
    }

    private fun launchExternalObserver(
        mockContext: Context,
        widgetId: Int,
        quotationsPreferencesDouble: QuotationsPreferences,
    ) {
        val quoteUnquoteWidgetSpy = spyk(QuoteUnquoteWidget())
        every {
            quoteUnquoteWidgetSpy.getQuotationsPreferences(
                mockContext,
                widgetId,
            )
        } returns quotationsPreferencesDouble

        quoteUnquoteWidgetSpy.launchExternalObserver(mockContext, 1, mockk())
    }
}
