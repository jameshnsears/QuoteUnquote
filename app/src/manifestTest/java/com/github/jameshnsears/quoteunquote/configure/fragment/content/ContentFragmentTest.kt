package com.github.jameshnsears.quoteunquote.configure.fragment.content

import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.BuildConfig
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ContentFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialContentSelections() {
        with(launchFragment<ContentFragmentDouble>()) {
            onFragment { fragment ->
                assertTrue(fragment.contentPreferences?.contentSelection == ContentSelection.ALL)
                assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.AUTHOR)
                assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.FAVOURITES)
                assertFalse(fragment.contentPreferences?.contentSelection == ContentSelection.SEARCH)

                if (BuildConfig.DEBUG) {
                    assertEquals("", "All: 7", fragment.fragmentContentBinding?.radioButtonAll?.text.toString())
                    assertEquals("", "Author: 1", fragment.fragmentContentBinding?.radioButtonAuthor?.text.toString())
                    assertEquals("", "Favourites: 0", fragment.fragmentContentBinding?.radioButtonFavourites?.text.toString())
                }

                assertTrue(fragment.fragmentContentBinding?.textViewLocalCodeValue?.text.toString().length == 10)
                assertEquals("", "", fragment.fragmentContentBinding?.editTextSearchText?.text.toString())

                fragment.shutdown()
            }
        }
    }

    @Test
    fun changeContentSelection() {
        with(launchFragment<ContentFragmentDouble>()) {
            onFragment { fragment ->
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

//    @Test
//    fun `todo - authorSearch`() {
//        fail("todo")
//    }
//
//    @Test
//    fun `todo - favouritesSend`() {
//        fail("todo")
//    }
//
//    @Test
//    fun `todo - favouritesReceive`() {
//        fail("todo")
//    }
}
