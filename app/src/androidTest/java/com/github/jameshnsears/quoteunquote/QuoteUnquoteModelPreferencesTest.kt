package com.github.jameshnsears.quoteunquote

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuoteUnquoteModelPreferencesTest : QuoteUnquoteModelUtility() {
    @Test
    fun initialPreferences() {
        insertTestDataSet01()

        assertEquals("", quoteUnquoteModelDouble.preferencesAuthorSearch(1))
        assertEquals("", quoteUnquoteModelDouble.preferencesTextSearch(1))
    }
}