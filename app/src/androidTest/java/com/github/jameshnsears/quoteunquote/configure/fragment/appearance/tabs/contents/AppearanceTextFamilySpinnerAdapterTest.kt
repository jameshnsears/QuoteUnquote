package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTextFamilySpinnerAdapterTest {
    @Test
    fun howManyFonts() {
        val appearanceFontSpinnerAdapter =
            AppearanceTextFamilySpinnerAdapter(getApplicationContext())

        assertEquals(6, appearanceFontSpinnerAdapter.count)
    }
}
