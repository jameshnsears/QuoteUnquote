package com.github.jameshnsears.quoteunquote.configure.fragment.notifications

import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import junit.framework.TestCase.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NotificationsFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialPreferences() {
        with(launchFragment<NotificationsFragmentDouble>()) {
            onFragment { fragment ->
                assertTrue(fragment.notificationsPreferences?.eventNextRandom == true)
                assertTrue(fragment.notificationsPreferences?.eventNextSequential == false)
                assertTrue(fragment.notificationsPreferences?.eventDisplayWidget == true)
                assertTrue(fragment.notificationsPreferences?.eventDisplayWidgetAndNotification == false)

                assertTrue(fragment.notificationsPreferences?.eventDeviceUnlock == false)

                assertTrue(fragment.notificationsPreferences?.eventDaily == false)
                assertThat(fragment.notificationsPreferences?.eventDailyTimeHour, IsEqual.equalTo(6))
                assertThat(fragment.notificationsPreferences?.eventDailyTimeMinute, IsEqual.equalTo(0))
            }
        }
    }

    @Test
    fun confirmChangesToPreferences() {
        with(launchFragment<NotificationsFragmentDouble>()) {
            onFragment { fragment ->
                assertThat(fragment.notificationsPreferences?.eventDailyTimeHour, IsEqual.equalTo(6))
                assertThat(fragment.notificationsPreferences?.eventDailyTimeMinute, IsEqual.equalTo(0))

                fragment.setDailyTime()
                fragment.notificationsPreferences?.eventDaily = true
                fragment.notificationsPreferences?.eventDailyTimeHour = 7
                fragment.notificationsPreferences?.eventDailyTimeMinute = 30

                assertThat(fragment.notificationsPreferences?.eventDailyTimeHour, IsEqual.equalTo(7))
                assertThat(fragment.notificationsPreferences?.eventDailyTimeMinute, IsEqual.equalTo(30))
            }
        }
    }
}
