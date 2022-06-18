package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.AppearanceTextStyleSpinnerAdapter
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
