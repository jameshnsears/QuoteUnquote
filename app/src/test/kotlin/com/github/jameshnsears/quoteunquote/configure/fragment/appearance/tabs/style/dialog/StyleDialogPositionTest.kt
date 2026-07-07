package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class StyleDialogPositionTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialPreferences() {
        launchFragment<StyleDialogPositionDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.setTextHide()
                assertThat(fragment.appearancePreferences?.appearancePositionTextHide, equalTo(true))
            }
        }
    }
}
