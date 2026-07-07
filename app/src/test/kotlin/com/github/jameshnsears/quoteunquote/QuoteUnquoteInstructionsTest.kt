package com.github.jameshnsears.quoteunquote

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = TestQuoteUnquoteApplication::class)
class QuoteUnquoteInstructionsTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onCreate_setsVersionText() {
        ActivityScenario.launch(QuoteUnquoteInstructions::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val textViewVersion = activity.findViewById<TextView>(R.id.textViewVersion)
                assertThat("textViewVersion should not be null", textViewVersion, notNullValue())
                val versionText = textViewVersion.text.toString()
                assertThat(
                    "Version text should contain VERSION_NAME",
                    versionText,
                    containsString(BuildConfig.VERSION_NAME),
                )
                assertThat(
                    "Version text should contain GIT_HASH",
                    versionText,
                    containsString(BuildConfig.GIT_HASH),
                )
            }
        }
    }

    @Test
    fun layoutFooter_click_startsGitHubIntent() {
        ActivityScenario.launch(QuoteUnquoteInstructions::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val layoutFooter = activity.findViewById<View>(R.id.layoutFooter)
                assertThat("layoutFooter should not be null", layoutFooter, notNullValue())
                layoutFooter.performClick()

                val expectedIntent =
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://github.com/jameshnsears/quoteunquote"))
                val actualIntent = shadowOf(activity).nextStartedActivity

                assertThat("actualIntent should not be null", actualIntent, notNullValue())
                assertThat(actualIntent.action, equalTo(expectedIntent.action))
                assertThat(actualIntent.data, equalTo(expectedIntent.data))
            }
        }
    }

    @Test
    fun onSaveInstanceState_savesScrollPosition() {
        ActivityScenario.launch(QuoteUnquoteInstructions::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val scrollView = activity.findViewById<ScrollView>(R.id.scrollViewInstructions)
                assertThat("scrollView should not be null", scrollView, notNullValue())
                scrollView.scrollY = 100

                val bundle = Bundle()
                ReflectionHelpers.callInstanceMethod<Any>(
                    activity,
                    "onSaveInstanceState",
                    ReflectionHelpers.ClassParameter.from(Bundle::class.java, bundle),
                )

                assertThat(bundle.getInt("scrollPositionY"), equalTo(100))
            }
        }
    }

    @Test
    fun onCreate_restoresScrollPosition() {
        val bundle = Bundle()
        bundle.putInt("scrollPositionY", 200)

        val activity =
            Robolectric
                .buildActivity(QuoteUnquoteInstructions::class.java)
                .create(bundle)
                .start()
                .resume()
                .get()

        val scrollView = activity.findViewById<ScrollView>(R.id.scrollViewInstructions)
        assertThat("scrollView should not be null", scrollView, notNullValue())

        ShadowLooper.idleMainLooper()

        val restoredScrollY = ReflectionHelpers.getField<Int>(activity, "scrollPositionY")
        assertThat(restoredScrollY, equalTo(200))
    }
}
