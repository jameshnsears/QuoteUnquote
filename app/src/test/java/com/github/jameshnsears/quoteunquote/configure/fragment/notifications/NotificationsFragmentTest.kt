package com.github.jameshnsears.quoteunquote.configure.fragment.notifications

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NotificationsFragmentTest {
    @Test
    fun setDailyAlarm() {
        val context: Context = getApplicationContext()

        assertFalse(isAlarmSet(context))

        val eventPreferences =
            NotificationsPreferences(
                WidgetIdHelper.WIDGET_ID_01,
                context
            )
        eventPreferences.eventDaily = true

        val eventDailyAlarm =
            NotificationsDailyAlarm(
                context,
                WidgetIdHelper.WIDGET_ID_01
            )
        eventDailyAlarm.setDailyAlarm()

        assertTrue(isAlarmSet(context))

        eventDailyAlarm.resetAnyExistingDailyAlarm()

        assertTrue(isAlarmSet(context))
    }

    private fun isAlarmSet(context: Context): Boolean {
        val intent = IntentFactoryHelper.createIntent(context, WidgetIdHelper.WIDGET_ID_01)
        intent.action = IntentFactoryHelper.DAILY_ALARM
        return PendingIntent.getBroadcast(
            context, WidgetIdHelper.WIDGET_ID_01, intent, PendingIntent.FLAG_NO_CREATE
        ) != null
    }
}
