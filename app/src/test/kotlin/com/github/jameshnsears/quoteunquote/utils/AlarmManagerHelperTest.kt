package com.github.jameshnsears.quoteunquote.utils

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AlarmManagerHelperTest {
    private lateinit var context: Context
    private lateinit var mockAlarmManager: AlarmManager

    @Before
    fun setUp() {
        context = spyk(ApplicationProvider.getApplicationContext<Context>())
        mockAlarmManager = mockk()
        every { context.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun canScheduleExactAlarms_returnsTrueOnApi31WhenGranted() {
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        assertThat(AlarmManagerHelper.canScheduleExactAlarms(context), `is`(true))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun canScheduleExactAlarms_returnsFalseOnApi31WhenNotGranted() {
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        assertThat(AlarmManagerHelper.canScheduleExactAlarms(context), `is`(false))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun canScheduleExactAlarms_handlesLiarApi() {
        // Simulate NoSuchMethodError
        every { mockAlarmManager.canScheduleExactAlarms() } throws NoSuchMethodError()

        assertThat(AlarmManagerHelper.canScheduleExactAlarms(context), `is`(true))
    }
}
