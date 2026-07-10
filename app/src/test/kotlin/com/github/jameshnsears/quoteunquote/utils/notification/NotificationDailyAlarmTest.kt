package com.github.jameshnsears.quoteunquote.utils.notification

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import com.github.jameshnsears.quoteunquote.utils.AlarmManagerHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class NotificationDailyAlarmTest {
    private lateinit var context: Context
    private lateinit var mockAlarmManager: AlarmManager
    private lateinit var mockNotificationsPreferences: NotificationsPreferences
    private val widgetId = 1

    @Before
    fun setUp() {
        context = spyk(ApplicationProvider.getApplicationContext())
        mockAlarmManager = mockk(relaxed = true)
        every { context.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        mockNotificationsPreferences = mockk(relaxed = true)
        mockkStatic(AlarmManagerHelper::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun setAlarm_permissionGranted_setsExactAlarm() {
        every { AlarmManagerHelper.canScheduleExactAlarms(any()) } returns true
        every { mockNotificationsPreferences.eventDaily } returns true

        val notificationDailyAlarm = object : NotificationDailyAlarm(context, widgetId) {
            init {
                // Injects mock via reflection or by overriding the field if possible.
                // Since it's protected and initialized in constructor, we might need to use reflection.
                val field = NotificationDailyAlarm::class.java.getDeclaredField("notificationsPreferences")
                field.isAccessible = true
                field.set(this, mockNotificationsPreferences)
            }
        }

        notificationDailyAlarm.setAlarm()

        verify { mockAlarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun setAlarm_permissionNotGranted_doesNotSetAlarm() {
        every { AlarmManagerHelper.canScheduleExactAlarms(any()) } returns false
        every { mockNotificationsPreferences.eventDaily } returns true

        val notificationDailyAlarm = object : NotificationDailyAlarm(context, widgetId) {
            init {
                val field = NotificationDailyAlarm::class.java.getDeclaredField("notificationsPreferences")
                field.isAccessible = true
                field.set(this, mockNotificationsPreferences)
            }
        }

        notificationDailyAlarm.setAlarm()

        verify(exactly = 0) { mockAlarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun setAlarm_securityException_fallsBackToInexact() {
        every { AlarmManagerHelper.canScheduleExactAlarms(any()) } returns true
        every { mockNotificationsPreferences.eventDaily } returns true
        every {
            mockAlarmManager.setExactAndAllowWhileIdle(any(), any(), any())
        } throws SecurityException("Exact alarm permission not granted")

        val notificationDailyAlarm = object : NotificationDailyAlarm(context, widgetId) {
            init {
                val field = NotificationDailyAlarm::class.java.getDeclaredField("notificationsPreferences")
                field.isAccessible = true
                field.set(this, mockNotificationsPreferences)
            }
        }

        notificationDailyAlarm.setAlarm()

        verify { mockAlarmManager.setAndAllowWhileIdle(any(), any(), any()) }
    }
}
