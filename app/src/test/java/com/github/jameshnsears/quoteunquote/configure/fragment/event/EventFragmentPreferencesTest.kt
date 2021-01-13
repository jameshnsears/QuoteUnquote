package com.github.jameshnsears.quoteunquote.configure.fragment.event

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
class EventFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun `confirm initial preferences`() {
        with(launchFragment<EventFragmentDouble>()) {
            onFragment { fragment ->
                assertTrue(fragment.eventPreferences?.eventDeviceUnlock == false)

                assertTrue(fragment.eventPreferences?.eventDaily == false)
                assertThat(fragment.eventPreferences?.eventDailyTimeHour, IsEqual.equalTo(6))
                assertThat(fragment.eventPreferences?.eventDailyTimeMinute, IsEqual.equalTo(0))
            }
        }
    }

    @Test
    fun `confirm changes to preferences`() {
        with(launchFragment<EventFragmentDouble>()) {
            onFragment { fragment ->
                fragment.eventPreferences?.eventDaily = true
                fragment.eventPreferences?.eventDailyTimeHour = 16
                fragment.eventPreferences?.eventDailyTimeMinute = 30

                fragment.setDailyTime()

                assertThat(fragment.eventPreferences?.eventDailyTimeHour, IsEqual.equalTo(16))
                assertThat(fragment.eventPreferences?.eventDailyTimeMinute, IsEqual.equalTo(30))
            }
        }
    }
}
