package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.selection

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.BuildConfig
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class QuotationsSelectionFragmentTest : ShadowLoggingHelper() {
    class QuotationsSelectionFragmentDouble : QuotationsSelectionFragment(WidgetIdHelper.WIDGET_ID_01) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val quoteUnquoteModel = mockk<QuoteUnquoteModel>()
            every { quoteUnquoteModel.countAll() } returns Single.just(7)
            every { quoteUnquoteModel.countFavouritesWithoutRx() } returns 1

            val authors = listOf(AuthorPOJO("a1"))
            every { quoteUnquoteModel.authors() } returns Single.just(authors)
            every { quoteUnquoteModel.authorsSorted(authors) } returns listOf("x", "a", "y")

            every { quoteUnquoteModel.authorsIndex(any()) } returns 0
            every { quoteUnquoteModel.countAuthorQuotations(any()) } returns 1
            every { quoteUnquoteModel.countFavourites() } returns Single.just(0)

            this.quoteUnquoteModel = quoteUnquoteModel
        }
    }

    private lateinit var scenario: FragmentScenario<QuotationsSelectionFragment>

    @Before
    fun before() {
        scenario = launchFragmentInContainer(
            Bundle(),
            R.style.FragmentScenarioEmptyFragmentActivityTheme,
            Lifecycle.State.RESUMED,
            object :
                FragmentFactory() {
                override fun instantiate(
                    classLoader: ClassLoader,
                    className: String
                ): Fragment {
                    return QuotationsSelectionFragmentDouble()
                }
            }
        )
    }

    @Test
    fun confirmInitialContentSelections() {
        scenario.onFragment { fragment ->
            assertTrue(fragment.quotationsPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.SEARCH)

            if (BuildConfig.DEBUG) {
                assertEquals(
                    "All: 7",
                    fragment.fragmentQuotationsTabSelectionBinding?.radioButtonAll?.text.toString()
                )
                assertEquals(
                    "Source: 1",
                    fragment.fragmentQuotationsTabSelectionBinding?.radioButtonAuthor?.text.toString()
                )
                assertEquals(
                    "Favourites: 0",
                    fragment.fragmentQuotationsTabSelectionBinding?.radioButtonFavourites?.text.toString()
                )
            }

            assertEquals(
                "",
                fragment.fragmentQuotationsTabSelectionBinding?.editTextSearchText?.text.toString()
            )

            fragment.shutdown()
        }
    }

    @Test
    fun changeContentSelection() {
        scenario.onFragment { fragment ->
            // default content selection
            assertTrue(fragment.quotationsPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.quotationsPreferences?.contentSelection = ContentSelection.AUTHOR
            fragment.setSelection()
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.ALL)
            assertTrue(fragment.quotationsPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.quotationsPreferences?.contentSelection = ContentSelection.FAVOURITES
            fragment.setSelection()
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertTrue(fragment.quotationsPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.quotationsPreferences?.contentSelection = ContentSelection.SEARCH
            fragment.setSelection()
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.quotationsPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertTrue(fragment.quotationsPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.shutdown()
        }
    }
}
