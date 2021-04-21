package com.github.jameshnsears.quoteunquote.configure

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ConfigureActivityTest {
    private fun getIntent(): Intent {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ConfigureActivity::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdHelper.WIDGET_ID_01)
        intent.putExtra("broadcastFinishIntent", false)
        return intent
    }

    @Test
    fun onBackPressed() {
        val scenario: ActivityScenario<ConfigureActivity> = launchActivity(getIntent())
        scenario.onActivity { activity ->
            activity.fragmentContent.fragmentContentBinding?.radioButtonSearch?.isChecked = true
            activity.onBackPressed()

            Assert.assertTrue(activity.fragmentContent.fragmentContentBinding?.radioButtonAll?.isChecked == true)
        }
        scenario.close()
    }

    @Test
    fun fragmentEventInitialState() {
        val scenario: ActivityScenario<ConfigureActivity> = launchActivity(getIntent())
        scenario.onActivity { activity ->
            val fragmentEvent = activity.supportFragmentManager.findFragmentById(R.id.fragmentPlaceholderEvent) as EventFragment
            Assert.assertFalse("", fragmentEvent.fragmentEventBinding!!.checkBoxDailyAt.isChecked)
            Assert.assertFalse("", fragmentEvent.fragmentEventBinding!!.checkBoxDeviceUnlock.isChecked)

            activity.fragmentContent.shutdown()
        }
        scenario.close()
    }
}
