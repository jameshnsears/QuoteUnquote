package com.github.jameshnsears.quoteunquote

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteUnquoteWidgetTest {
    @Test
    fun getTransparencyMask() {
        val quoteUnquoteWidgetSpy = QuoteUnquoteWidget()
        assertEquals(-436207617, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFFFFFF"))
        assertEquals(-452984832, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF000000"))
    }
}
