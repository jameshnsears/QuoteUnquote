package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class BrowseSearchDialogFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialState() {
        val quoteUnquoteModel = mockk<QuoteUnquoteModel>()
        every { quoteUnquoteModel.favourites } returns emptyList()
        every { quoteUnquoteModel.getSearchQuotations(any(), any(), any()) } returns emptyList()
        every { quoteUnquoteModel.getSearchQuotationsRegEx(any(), any(), any()) } returns emptyList()

        launchFragment(themeResId = R.style.AppTheme) {
            BrowseSearchDialogFragmentDouble(quoteUnquoteModel)
        }.use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(
                    fragment.fragmentQuotationsTabFilterBrowseDialogBinding?.recycleViewBrowse,
                    notNullValue(),
                )
            }
        }
    }
}
