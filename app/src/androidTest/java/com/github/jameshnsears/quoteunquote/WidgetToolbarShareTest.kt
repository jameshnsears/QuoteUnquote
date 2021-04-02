package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Test

class WidgetToolbarShareTest : QuoteUnquoteModelUtility() {
    @Test
    fun onReceiveToolbarPressedShare() {
        insertQuotationTestData01()

        val quoteUnquoteWidget = QuoteUnquoteWidget()
        quoteUnquoteWidget.quoteUnquoteModel = quoteUnquoteModelDouble
        quoteUnquoteWidget.quoteUnquoteModel?.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)
        quoteUnquoteWidget.onReceiveToolbarPressedShare(context, WidgetIdHelper.WIDGET_ID_01)
    }
}
