package com.github.jameshnsears.quoteunquote

import android.os.Build
import android.widget.RemoteViews
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class WidgetHeartColourTest : QuoteUnquoteModelUtility() {
    @Test
    fun setHeartColour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            insertQuotationTestData01()

            val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
            every { quoteUnquoteWidget.getQuoteUnquoteModel(any()) } returns quoteUnquoteModelDouble

            quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

            val remoteViews = mockk<RemoteViews>()
            every { remoteViews.setImageViewResource(any(), any()) } returns Unit

            quoteUnquoteWidget.onEnabled(context)
            quoteUnquoteWidget.setHeartColour(
                context,
                WidgetIdHelper.WIDGET_ID_01,
                remoteViews
            )
        }
    }
}
