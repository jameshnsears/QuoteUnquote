package com.github.jameshnsears.quoteunquote.configure.fragment.schedule

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
class ScheduleFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialPreferences() {
        with(launchFragment<ScheduleFragmentDouble>()) {
            onFragment { fragment ->
                assertTrue(fragment.schedulePreferences?.eventNextRandom == true)
                assertTrue(fragment.schedulePreferences?.eventNextSequential == false)
                assertTrue(fragment.schedulePreferences?.eventDisplayWidget == true)
                assertTrue(fragment.schedulePreferences?.eventDisplayWidgetAndNotification == false)

                assertTrue(fragment.schedulePreferences?.eventDeviceUnlock == false)

                assertTrue(fragment.schedulePreferences?.eventDaily == false)
                assertThat(fragment.schedulePreferences?.eventDailyTimeHour, IsEqual.equalTo(6))
                assertThat(fragment.schedulePreferences?.eventDailyTimeMinute, IsEqual.equalTo(0))
            }
        }
    }

    @Test
    fun confirmChangesToPreferences() {
        with(launchFragment<ScheduleFragmentDouble>()) {
            onFragment { fragment ->
                assertThat(fragment.schedulePreferences?.eventDailyTimeHour, IsEqual.equalTo(6))
                assertThat(fragment.schedulePreferences?.eventDailyTimeMinute, IsEqual.equalTo(0))

                fragment.setDailyTime()
                fragment.schedulePreferences?.eventDaily = true
                fragment.schedulePreferences?.eventDailyTimeHour = 7
                fragment.schedulePreferences?.eventDailyTimeMinute = 30

                assertThat(fragment.schedulePreferences?.eventDailyTimeHour, IsEqual.equalTo(7))
                assertThat(fragment.schedulePreferences?.eventDailyTimeMinute, IsEqual.equalTo(30))
            }
        }
    }
}
