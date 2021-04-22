package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTextStyleSpinnerAdapterTest {
    @Test
    fun howManyFonts() {
        val appearanceStyleSpinnerAdapter = AppearanceTextStyleSpinnerAdapter(getApplicationContext())

        assertEquals(4, appearanceStyleSpinnerAdapter.count)
    }
}
