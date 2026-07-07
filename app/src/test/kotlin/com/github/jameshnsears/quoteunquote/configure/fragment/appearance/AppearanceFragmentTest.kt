package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

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
class AppearanceFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialTab() {
        launchFragment<AppearanceFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(
                    fragment.fragmentAppearanceBinding?.viewPager2Appearance?.currentItem,
                    equalTo(0),
                )
            }
        }
    }
}
