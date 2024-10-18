package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.fortune

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class FilesFortuneTest : ShadowLoggingHelper() {
    @Test
    fun importFortune() {
        assertFalse( 1 == 2)
    }
}
