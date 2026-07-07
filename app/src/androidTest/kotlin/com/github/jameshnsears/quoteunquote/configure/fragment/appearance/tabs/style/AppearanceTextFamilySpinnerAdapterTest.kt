package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AppearanceTextFamilySpinnerAdapterTest {
    @Test
    fun howManyFonts() {
        val appearanceFontSpinnerAdapter =
            AppearanceTextFamilySpinnerAdapter(getApplicationContext())

        assertThat(appearanceFontSpinnerAdapter.count, equalTo(6))
    }
}
