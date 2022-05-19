package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.cloud.transfer.Appearance
import com.github.jameshnsears.quoteunquote.cloud.transfer.GsonTestHelper
import com.github.jameshnsears.quoteunquote.cloud.transfer.Quotations
import com.github.jameshnsears.quoteunquote.cloud.transfer.Schedule
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TransferBackupSettingsTest : GsonTestHelper() {
    @Test
    fun settingsQuotations() {
        assertEquals(
            gson().toJson(expectedQuotations()),
            gson().toJson(
                TransferBackupSettings(context).settingsQuotations(
                    1, getApplicationContext()
                )
            )
        )
    }

    @Test
    fun settingsAppearance() {
        assertEquals(
            gson().toJson(expectedAppearance()),
            gson().toJson(
                TransferBackupSettings(context).settingsAppearance(
                    1, getApplicationContext()
                )
            )
        )
    }

    @Test
    fun settingsSchedule() {
        assertEquals(
            gson().toJson(expectedSchedule()),
            gson().toJson(
                TransferBackupSettings(context).settingsSchedule(
                    1, getApplicationContext()
                )
            )
        )
    }

    @Test
    fun settings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

            assertEquals(
                gson().toJson(expectedSettings()),
                gson().toJson(TransferBackupSettings(context).settings())
            )
        }
    }

    fun expectedSettings(): List<Settings> {
        val settingsList = mutableListOf<Settings>()

        settingsList.add(
            Settings(
                expectedQuotations(),
                expectedAppearance(),
                expectedSchedule(),
                1
            )
        )

        return settingsList
    }

    private fun expectedSchedule() = Schedule(
        true,
        false,
        true,
        false,
        false,
        false,
        -1,
        -1
    )

    private fun expectedAppearance() = Appearance(
        -1,
        "#FFF8FD89",
        "Sans Serif",
        "Regular",
        16,
        "#FF000000",
        "",
        false,
        true,
        true,
        true,
        true,
        false
    )

    private fun expectedQuotations() = Quotations(
        true,
        true,
        false,
        "",
        false,
        false,
        -1,
        ""
    )
}