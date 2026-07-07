package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.app.Application
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class ContentExternalFileFragmentTest : ShadowLoggingHelper() {
    @Test
    fun initButtons_enabled() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        every { mockPrefs.databaseExternalCsv } returns true
        every { mockModel.externalDatabaseContainsQuotations() } returns true

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentExternalFileFragmentDouble::class.java.name) {
                        ContentExternalFileFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentExternalFileFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(fragment.binding.radioButtonDatabaseExternalFile.isEnabled, `is`(true))
                assertThat(fragment.binding.radioButtonDatabaseExternalFile.isChecked, `is`(true))
                assertThat(fragment.binding.buttonExport.isEnabled, `is`(true))
                assertThat(fragment.binding.buttonEdit.isEnabled, `is`(true))
            }
        }
    }

    @Test
    fun initButtons_disabled() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        every { mockPrefs.databaseExternalCsv } returns false
        every { mockModel.externalDatabaseContainsQuotations() } returns false

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentExternalFileFragmentDouble::class.java.name) {
                        ContentExternalFileFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentExternalFileFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(fragment.binding.radioButtonDatabaseExternalFile.isChecked, `is`(false))
                assertThat(fragment.binding.buttonExport.isEnabled, `is`(false))
                assertThat(fragment.binding.buttonEdit.isEnabled, `is`(false))
            }
        }
    }

    @Test
    fun radioSelectionChangesPreferences() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        mockPrefs.databaseExternalCsv = false
        every { mockModel.externalDatabaseContainsQuotations() } returns true

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentExternalFileFragmentDouble::class.java.name) {
                        ContentExternalFileFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentExternalFileFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                // Simulate clicking the radio button
                fragment.binding.radioButtonDatabaseExternalFile.isChecked = true

                assertThat(mockPrefs.databaseExternalCsv, `is`(true))
                assertThat(mockPrefs.databaseInternal, `is`(false))
                assertThat(mockPrefs.databaseExternalWeb, `is`(false))

                assertThat(fragment.binding.buttonExport.isEnabled, `is`(true))
                assertThat(fragment.binding.buttonEdit.isEnabled, `is`(true))
            }
        }
    }

    @Test
    fun uncheckingRadioDoesNotReenableButtons() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        every { mockModel.externalDatabaseContainsQuotations() } returns true

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentExternalFileFragmentDouble::class.java.name) {
                        ContentExternalFileFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentExternalFileFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                // First check it - should enable buttons
                fragment.binding.radioButtonDatabaseExternalFile.isChecked = true
                assertThat(fragment.binding.buttonEdit.isEnabled, `is`(true))

                // Now simulate unchecking it by code (as done in dialogDismissed or initButtons(false))
                // We manually set button disabled first, then uncheck radio
                fragment.binding.buttonEdit.isEnabled = false
                fragment.binding.radioButtonDatabaseExternalFile.isChecked = false

                // The button should REMAIN disabled.
                // If the bug were present, the listener would have re-enabled it.
                assertThat(fragment.binding.buttonEdit.isEnabled, `is`(false))
            }
        }
    }
}
