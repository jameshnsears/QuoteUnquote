package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceStyleSpinnerAdapterTest {
    @Test
    fun howManyFonts() {
        val appearanceStyleSpinnerAdapter = AppearanceStyleSpinnerAdapter(getApplicationContext())

        assertEquals(4, appearanceStyleSpinnerAdapter.count)
    }
}
