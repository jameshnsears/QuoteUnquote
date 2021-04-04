package com.github.jameshnsears.quoteunquote.configure.fragment.content

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ContentFragmentAuthorTest {
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
                    return ContentFragmentTest.ContentFragmentDouble()
                }
            }
        )
    }

    @Test
    fun setAuthor() {
        scenario.onFragment { fragment ->
            assertEquals("x", fragment.contentPreferences?.contentSelectionAuthor)
            fragment.fragmentContentBinding?.spinnerAuthors?.setSelection(1)
            assertEquals("a", fragment.contentPreferences?.contentSelectionAuthor)
            fragment.shutdown()
        }

        // visit settings page again
        scenario.onFragment { fragment ->
            assertEquals("a", fragment.fragmentContentBinding?.spinnerAuthors?.selectedItem)
            fragment.shutdown()
        }
    }
}
