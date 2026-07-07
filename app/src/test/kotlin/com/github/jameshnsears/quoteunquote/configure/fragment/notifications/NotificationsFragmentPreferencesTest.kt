package com.github.jameshnsears.quoteunquote.configure.fragment.notifications

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class NotificationsFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialPreferences() {
        launchFragment<NotificationsFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(fragment.notificationsPreferences?.eventNextRandom, equalTo(false))
                assertThat(fragment.notificationsPreferences?.eventNextSequential, equalTo(true))
                assertThat(fragment.notificationsPreferences?.eventDisplayWidget, equalTo(true))
                assertThat(
                    fragment.notificationsPreferences?.eventDisplayWidgetAndNotification,
                    equalTo(false),
                )

                assertThat(fragment.notificationsPreferences?.eventDeviceUnlock, equalTo(false))

                assertThat(fragment.notificationsPreferences?.eventDaily, equalTo(false))
                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeHour,
                    equalTo(6),
                )
                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeMinute,
                    equalTo(0),
                )
                assertThat(fragment.notificationsPreferences?.customisableInterval, equalTo(false))
            }
        }
    }

    @Test
    fun confirmChangesToPreferences() {
        launchFragment<NotificationsFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeHour,
                    equalTo(6),
                )
                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeMinute,
                    equalTo(0),
                )

                fragment.notificationsPreferences?.eventDaily = true
                fragment.notificationsPreferences?.eventDailyTimeHour = 7
                fragment.notificationsPreferences?.eventDailyTimeMinute = 30

                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeHour,
                    equalTo(7),
                )
                assertThat(
                    fragment.notificationsPreferences?.eventDailyTimeMinute,
                    equalTo(30),
                )
            }
        }
    }
}
