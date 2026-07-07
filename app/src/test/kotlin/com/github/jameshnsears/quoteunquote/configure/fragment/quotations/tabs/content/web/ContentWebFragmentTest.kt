package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.web

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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class ContentWebFragmentTest : ShadowLoggingHelper() {
    @Test
    fun initUI_fromPreferences() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        every { mockPrefs.databaseExternalWeb } returns true
        every { mockPrefs.databaseWebUrl } returns "https://example.com"
        every { mockPrefs.databaseWebXpathQuotation } returns "//p[@class='quote']"
        every { mockPrefs.databaseWebXpathSource } returns "//p[@class='author']"
        every { mockPrefs.databaseWebKeepLatestOnly } returns true

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentWebFragmentDouble::class.java.name) {
                        ContentWebFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentWebFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                val binding = fragment.fragmentQuotationsTabDatabaseTabWebBinding!!
                assertThat(binding.radioButtonDatabaseExternalWeb.isChecked, `is`(true))
                assertThat(binding.radioButtonDatabaseExternalWeb.isEnabled, `is`(true))
                assertThat(binding.editTextUrl.text.toString(), equalTo("https://example.com"))
                assertThat(binding.editTextXpathQuotation.text.toString(), equalTo("//p[@class='quote']"))
                assertThat(binding.editTextXpathSource.text.toString(), equalTo("//p[@class='author']"))
                assertThat(binding.switchKeepLatestResponseOnly.isChecked, `is`(true))
            }
        }
    }

    @Test
    fun radioSelectionChangesPreferences() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        mockPrefs.databaseExternalWeb = false

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentWebFragmentDouble::class.java.name) {
                        ContentWebFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentWebFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                val binding = fragment.fragmentQuotationsTabDatabaseTabWebBinding!!

                // Simulate clicking the radio button
                binding.radioButtonDatabaseExternalWeb.isChecked = true

                verify { mockPrefs.databaseExternalWeb = true }
                verify { mockPrefs.databaseInternal = false }
                verify { mockPrefs.databaseExternalCsv = false }
            }
        }
    }

    @Test
    fun editTextFocusChangeUpdatesPreferences() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentWebFragmentDouble::class.java.name) {
                        ContentWebFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentWebFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                val binding = fragment.fragmentQuotationsTabDatabaseTabWebBinding!!

                binding.editTextUrl.setText("https://newurl.com")
                binding.editTextUrl.onFocusChangeListener.onFocusChange(binding.editTextUrl, false)
                verify { mockPrefs.databaseWebUrl = "https://newurl.com" }

                binding.editTextXpathQuotation.setText("//div")
                binding.editTextXpathQuotation.onFocusChangeListener.onFocusChange(binding.editTextXpathQuotation, false)
                verify { mockPrefs.databaseWebXpathQuotation = "//div" }

                binding.editTextXpathSource.setText("//span")
                binding.editTextXpathSource.onFocusChangeListener.onFocusChange(binding.editTextXpathSource, false)
                verify { mockPrefs.databaseWebXpathSource = "//span" }
            }
        }
    }

    @Test
    fun switchKeepLatestUpdatesPreferences() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        every { mockPrefs.databaseWebKeepLatestOnly } returns false
        every { mockPrefs.databaseWebKeepLatestOnly = any() } just Runs

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentWebFragmentDouble::class.java.name) {
                        ContentWebFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentWebFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                val binding = fragment.fragmentQuotationsTabDatabaseTabWebBinding!!

                binding.switchKeepLatestResponseOnly.isChecked = true
                verify { mockPrefs.databaseWebKeepLatestOnly = true }

                binding.switchKeepLatestResponseOnly.isChecked = false
                verify { mockPrefs.databaseWebKeepLatestOnly = false }
            }
        }
    }

    @Test
    fun buttonImportWebPage_success() {
        val mockModel = mockk<QuoteUnquoteModel>(relaxed = true)
        val mockPrefs = spyk(QuotationsPreferences(WidgetIdHelper.WIDGET_ID_01, ApplicationProvider.getApplicationContext()))

        val fragmentFactory =
            object : FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String,
                ): Fragment =
                    if (className == ContentWebFragmentDouble::class.java.name) {
                        ContentWebFragmentDouble().apply {
                            this.quoteUnquoteModel = mockModel
                            this.quotationsPreferences = mockPrefs
                        }
                    } else {
                        super.instantiate(classLoader, className)
                    }
            }

        launchFragment<ContentWebFragmentDouble>(
            themeResId = R.style.AppTheme,
            factory = fragmentFactory,
        ).use { scenario ->
            scenario.onFragment { fragment ->
                val binding = fragment.fragmentQuotationsTabDatabaseTabWebBinding!!

                binding.editTextUrl.setText("https://example.com/quotes")
                binding.editTextXpathQuotation.setText("//quote")
                binding.editTextXpathSource.setText("//author")

                // Mock scraper success
                val scraperData = mockk<com.github.jameshnsears.quoteunquote.scraper.ScraperData>()
                every { scraperData.scrapeResult } returns true
                every { scraperData.quotation } returns "A quote"
                every { scraperData.source } returns "An author"
                every { mockModel.getWebPage(any(), any(), any(), any()) } returns scraperData

                // Simulate button press
                binding.buttonImportWebPage.isPressed = true
                binding.buttonImportWebPage.performClick()

                verify { mockModel.getWebPage(any(), "https://example.com/quotes", "//quote", "//author") }
                verify { mockModel.insertWebPage(any(), "A quote", "An author", any()) }
                verify { mockPrefs.databaseExternalWeb = true }
            }
        }
    }
}
