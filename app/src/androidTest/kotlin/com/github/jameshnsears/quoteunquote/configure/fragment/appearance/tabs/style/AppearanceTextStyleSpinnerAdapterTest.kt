package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AppearanceTextStyleSpinnerAdapterTest {
    @Test
    fun howManyTextStyles() {
        val appearanceStyleSpinnerAdapter =
            AppearanceTextStyleSpinnerAdapter(getApplicationContext())

        assertThat(appearanceStyleSpinnerAdapter.count, equalTo(6))
    }
}
