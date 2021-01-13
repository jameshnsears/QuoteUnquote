package com.github.jameshnsears.quoteunquote.configure

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.WidgetIdTestHelper
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ConfigureActivityTest : ShadowLoggingHelper() {
    private lateinit var scenario: ActivityScenario<ConfigureActivityDouble>

    private fun getIntent(): Intent {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ConfigureActivityDouble::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdTestHelper.WIDGET_ID)
        return intent
    }

    @Before
    fun before() {
        scenario = launchActivity(getIntent())
    }

    @After
    fun after() {
        scenario.close()
    }

    @Test
    fun emptySearchResultsThenBackPressed() {
        scenario.onActivity { activity ->
            activity.fragmentContent.fragmentContentBinding?.radioButtonSearch?.isChecked = true
            activity.onBackPressed()

            assertTrue(activity.fragmentContent.fragmentContentBinding?.radioButtonAll?.isChecked == true)
        }
    }

    @Test
    fun fragmentEvent() {
        scenario.onActivity { activity ->
            val fragmentEvent = activity.supportFragmentManager.findFragmentById(R.id.fragmentPlaceholderEvent) as EventFragment
            assertFalse("", fragmentEvent.fragmentEventBinding!!.checkBoxDailyAt.isChecked)
            assertFalse("", fragmentEvent.fragmentEventBinding!!.checkBoxDeviceUnlock.isChecked)
        }
    }
}
