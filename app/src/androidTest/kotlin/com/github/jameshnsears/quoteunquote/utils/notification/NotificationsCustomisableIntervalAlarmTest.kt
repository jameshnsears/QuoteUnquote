package com.github.jameshnsears.quoteunquote.utils.notification

import android.content.Context
import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SdkSuppress
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivityDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test

@SdkSuppress(maxSdkVersion = Build.VERSION_CODES.R)
class NotificationsCustomisableIntervalAlarmTest : QuoteUnquoteModelUtility() {
    @Rule
    @JvmField
    var rule = ActivityScenarioRule(ConfigureActivityDouble::class.java)

    private fun setPreferences(
        hourFrom: Int,
        hourTo: Int,
        hours: Int,
    ) {
        val notificationsPreferences =
            NotificationsPreferences(WidgetIdHelper.WIDGET_ID_01, context)
        notificationsPreferences.customisableInterval = true
        notificationsPreferences.customisableIntervalHourFrom = hourFrom
        notificationsPreferences.customisableIntervalHourTo = hourTo
        notificationsPreferences.customisableIntervalHours = hours
    }

    class AlarmMock(context: Context, val current: Int) : NotificationCustomisableIntervalAlarm(
        context,
        WidgetIdHelper.WIDGET_ID_01,
    ) {
        override fun currentHour() = current
    }

    private fun assertion(
        from: Int,
        to: Int,
        hours: Int,
        current: Int,
        nextAlarmHour: Int,
        nextAlarmDay: Boolean,
    ) {
        val alarm = AlarmMock(context, current)
        setPreferences(from, to, hours)

        alarm.setAlarm()

        assertThat(alarm.nextAlarmHour, `is`(nextAlarmHour))
        assertThat(alarm.nextAlarmDay, `is`(nextAlarmDay))

        alarm.resetAlarm()
    }

    @Test
    fun range() {
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 5,
            nextAlarmHour = 6,
            nextAlarmDay = false,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 6,
            nextAlarmHour = 10,
            nextAlarmDay = false,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 7,
            nextAlarmHour = 10,
            nextAlarmDay = false,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 8,
            nextAlarmHour = 10,
            nextAlarmDay = false,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 9,
            nextAlarmHour = 10,
            nextAlarmDay = false,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 10,
            nextAlarmHour = 6,
            nextAlarmDay = true,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 11,
            nextAlarmHour = 6,
            nextAlarmDay = true,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 12,
            nextAlarmHour = 6,
            nextAlarmDay = true,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 13,
            nextAlarmHour = 6,
            nextAlarmDay = true,
        )
        assertion(
            from = 6,
            to = 12,
            hours = 4,
            current = 10,
            nextAlarmHour = 6,
            nextAlarmDay = true,
        )
    }

    @Test
    fun from9To21current0() {
        assertion(
            from = 9,
            to = 21,
            hours = 1,
            current = 0,
            nextAlarmHour = 9,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To21current0hours3() {
        assertion(
            from = 9,
            to = 21,
            hours = 3,
            current = 10,
            nextAlarmHour = 12,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To9current8() {
        assertion(
            from = 9,
            to = 9,
            hours = 1,
            current = 8,
            nextAlarmHour = 9,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To9current8hours4() {
        assertion(
            from = 9,
            to = 9,
            hours = 4,
            current = 8,
            nextAlarmHour = 9,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To21current9() {
        assertion(
            from = 9,
            to = 21,
            hours = 1,
            current = 9,
            nextAlarmHour = 10,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To21current9hours4() {
        assertion(
            from = 9,
            to = 21,
            hours = 4,
            current = 9,
            nextAlarmHour = 13,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To21current20() {
        assertion(
            from = 9,
            to = 21,
            hours = 1,
            current = 20,
            nextAlarmHour = 21,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To21current12Hours2() {
        assertion(
            from = 9,
            to = 21,
            hours = 2,
            current = 12,
            nextAlarmHour = 13,
            nextAlarmDay = false,
        )
    }

    @Test
    fun from9To9current9() {
        assertion(
            from = 9,
            to = 9,
            hours = 1,
            current = 9,
            nextAlarmHour = 9,
            nextAlarmDay = true,
        )
    }

    @Test
    fun from9To21current21() {
        assertion(
            from = 9,
            to = 21,
            hours = 1,
            current = 21,
            nextAlarmHour = 9,
            nextAlarmDay = true,
        )
    }

    @Test
    fun from0To23current23() {
        assertion(
            from = 0,
            to = 23,
            hours = 1,
            current = 23,
            nextAlarmHour = 0,
            nextAlarmDay = true,
        )
    }

    @Test
    fun from23To23current23() {
        assertion(
            from = 23,
            to = 23,
            hours = 1,
            current = 23,
            nextAlarmHour = 23,
            nextAlarmDay = true,
        )
    }
}
