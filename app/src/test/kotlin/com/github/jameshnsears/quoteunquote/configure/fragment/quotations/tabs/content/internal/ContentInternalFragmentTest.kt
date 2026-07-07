package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.internal

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
class ContentInternalFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialPreferences() {
        launchFragment<ContentInternalFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(
                    fragment.fragmentQuotationsTabDatabaseTabInternalBinding?.radioButtonDatabaseInternal?.isChecked,
                    equalTo(true),
                )
            }
        }
    }

    @Test
    fun radioButtonDatabaseInternalChecked() {
        launchFragment<ContentInternalFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabDatabaseTabInternalBinding?.radioButtonDatabaseInternal?.isChecked = true
                assertThat(fragment.quotationsPreferences?.databaseInternal, equalTo(true))
            }
        }
    }
}
