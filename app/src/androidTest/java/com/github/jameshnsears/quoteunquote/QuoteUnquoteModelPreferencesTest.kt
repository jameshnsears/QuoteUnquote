package com.github.jameshnsears.quoteunquote

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteUnquoteModelPreferencesTest : QuoteUnquoteModelUtility() {
    @Test
    fun initialPreferences() {
        insertTestDataSet01()

        assertEquals("", quoteUnquoteModelDouble.preferencesAuthorSearch(1))
        assertEquals("q1", quoteUnquoteModelDouble.preferencesTextSearch(1))
    }
}