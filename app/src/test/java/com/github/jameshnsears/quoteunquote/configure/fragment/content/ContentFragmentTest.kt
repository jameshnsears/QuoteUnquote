package com.github.jameshnsears.quoteunquote.configure.fragment.content

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
class ContentFragmentTest : ShadowLoggingHelper() {
    class ContentFragmentDouble : ContentFragment(WidgetIdHelper.WIDGET_ID_01) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val quoteUnquoteModel = mockk<QuoteUnquoteModel>()
            every { quoteUnquoteModel.countAll() } returns Single.just(7)

            val authors = listOf(AuthorPOJO("a1"))
            every { quoteUnquoteModel.authors() } returns Single.just(authors)
            every { quoteUnquoteModel.authorsSorted(authors) } returns listOf("x", "a", "y")

            every { quoteUnquoteModel.authorsIndex(any()) } returns 0
            every { quoteUnquoteModel.countAuthorQuotations(any()) } returns 1
            every { quoteUnquoteModel.countFavourites() } returns Single.just(0)

            this.quoteUnquoteModel = quoteUnquoteModel
        }
    }

    private lateinit var scenario: FragmentScenario<ContentFragment>

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
                    return ContentFragmentDouble()
                }
            }
        )
    }

    @Test
    fun confirmInitialContentSelections() {
        scenario.onFragment { fragment ->
            assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

            if (BuildConfig.DEBUG) {
                assertEquals("All: 7", fragment.fragmentContentBinding?.radioButtonAll?.text.toString())
                assertEquals("Author: 1", fragment.fragmentContentBinding?.radioButtonAuthor?.text.toString())
                assertEquals("Favourite: 0", fragment.fragmentContentBinding?.radioButtonFavourites?.text.toString())
            }

            assertEquals("", fragment.fragmentContentBinding?.editTextSearchText?.text.toString())

            fragment.shutdown()
        }
    }

    @Test
    fun changeContentSelection() {
        scenario.onFragment { fragment ->
            // default content selection
            assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.contentPreferences?.contentSelection = ContentSelection.AUTHOR
            fragment.setSelection()
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
            assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.contentPreferences?.contentSelection = ContentSelection.FAVOURITES
            fragment.setSelection()
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.contentPreferences?.contentSelection = ContentSelection.SEARCH
            fragment.setSelection()
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
            assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
            assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

            fragment.shutdown()
        }
    }
}
