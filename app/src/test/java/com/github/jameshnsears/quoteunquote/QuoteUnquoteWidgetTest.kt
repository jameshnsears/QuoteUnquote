package com.github.jameshnsears.quoteunquote

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteUnquoteWidgetTest {
    @Test
    fun getTransparencyMask() {
        val quoteUnquoteWidgetSpy = QuoteUnquoteWidget()
        assertEquals(-436207617, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFFFFFF"))
        assertEquals(-436666999, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFF8FD89"))
        assertEquals(-436207872, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFFFF00"))
        assertEquals(-436218112, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFFD700"))
        assertEquals(-436235435, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFF9355"))
        assertEquals(-436236911, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFF8D91"))
        assertEquals(-436267885, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFFF1493"))
        assertEquals(-437399120, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFEDD1B0"))
        assertEquals(-440352576, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFC0C0C0"))
        assertEquals(-440825892, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FFB987DC"))
        assertEquals(-443322149, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF9370DB"))
        assertEquals(-444596096, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF800080"))
        assertEquals(-445004675, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF79C47D"))
        assertEquals(-446310693, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF65D6DB"))
        assertEquals(-447364123, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF55C3E5"))
        assertEquals(-452984832, quoteUnquoteWidgetSpy.getTransparencyMask(1, "#FF000000"))
    }
}
