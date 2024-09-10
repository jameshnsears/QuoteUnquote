package espresso

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.BuildConfig
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivityDouble
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class EspressoConfigurationTest {
    @Rule
    @JvmField
    var rule = ActivityScenarioRule(ConfigureActivityDouble::class.java)

    private fun canTestBeRun(): Boolean {
        return BuildConfig.FLAVOR.equals("espresso")
    }

    @Test
    fun pressQuotations() {
        if (canTestBeRun()) {
            onView(withId(R.id.navigationBarQuotations))
                .waitUntilVisible(2500)
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun pressAppearance() {
        if (canTestBeRun()) {
            onView(withId(R.id.navigationBarAppearance))
                .waitUntilVisible(2500)
                .perform(click())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun pressNotifications() {
        if (canTestBeRun()) {
            onView(withId(R.id.navigationBarNotification))
                .waitUntilVisible(2500)
                .perform(click())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun pressSync() {
        if (canTestBeRun()) {
            onView(withId(R.id.navigationBarSync))
                .waitUntilVisible(2500)
                .perform(click())
                .check(matches(isDisplayed()))

            onView(withId(R.id.radioButtonSyncGoogleCloud))
                .check(matches(isChecked()))

            onView(withId(R.id.radioButtonSyncDevice))
                .check(matches(isNotChecked()))

            onView(withId(R.id.buttonBackup))
                .check(matches(isEnabled()))

            onView(withId(R.id.buttonRestore))
                .check(matches(isEnabled()))

            val textViewLocalCodeValue = onView(withId(R.id.textViewLocalCodeValue))
            assertTrue(getText(textViewLocalCodeValue).length == 10)

            onView(withId(R.id.textViewLocalCodeValue))
                .check(matches(isDisplayed()))

            onView(withId(R.id.editTextRemoteCodeValue))
                .check(matches(withText("")))
        }
    }

    private fun ViewInteraction.waitUntilVisible(timeout: Long): ViewInteraction {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeout

        do {
            try {
                check(matches(isDisplayed()))
                return this
            } catch (e: AssertionFailedError) {
                Thread.sleep(50)
            }
        } while (System.currentTimeMillis() < endTime)

        throw TimeoutException()
    }

    private fun getText(matcher: ViewInteraction): String {
        var text = String()
        matcher.perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "Text of the view"
            }

            override fun perform(uiController: UiController, view: View) {
                val tv = view as TextView
                text = tv.text.toString()
            }
        })

        return text
    }
}
