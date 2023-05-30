package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTextStyleSpinnerAdapterTest {
    @Test
    fun howManyTextStyles() {
        val appearanceStyleSpinnerAdapter =
            AppearanceTextStyleSpinnerAdapter(getApplicationContext())

        assertEquals(6, appearanceStyleSpinnerAdapter.count)
    }
}
