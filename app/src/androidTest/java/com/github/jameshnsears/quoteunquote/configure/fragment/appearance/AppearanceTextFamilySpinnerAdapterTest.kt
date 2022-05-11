package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.AppearanceTextFamilySpinnerAdapter
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTextFamilySpinnerAdapterTest {
    @Test
    fun howManyFonts() {
        val appearanceFontSpinnerAdapter =
            AppearanceTextFamilySpinnerAdapter(getApplicationContext())

        // https://www.cs.cmu.edu/~jbigham/pubs/pdfs/2017/colors.pdf

        assertEquals(6, appearanceFontSpinnerAdapter.count)
    }
}
