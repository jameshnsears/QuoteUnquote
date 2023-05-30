package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

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
                    1,
                    getApplicationContext(),
                ),
            ),
        )
    }

    @Test
    fun settingsAppearance() {
        assertEquals(
            gson().toJson(expectedAppearance()),
            gson().toJson(
                TransferBackupSettings(context).settingsAppearance(
                    1,
                    getApplicationContext(),
                ),
            ),
        )
    }

    @Test
    fun settingsSchedule() {
        assertEquals(
            gson().toJson(expectedSchedule()),
            gson().toJson(
                TransferBackupSettings(context).settingsSchedule(
                    1,
                    getApplicationContext(),
                ),
            ),
        )
    }

    @Test
    fun settings() {
        if (canWorkWithMockk()) {
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

            assertEquals(
                gson().toJson(expectedSettings()),
                gson().toJson(TransferBackupSettings(context).settings()),
            )
        }
    }

    private fun expectedSettings(): List<Settings> {
        val settingsList = mutableListOf<Settings>()

        settingsList.add(
            Settings(
                expectedQuotations(),
                expectedAppearance(),
                expectedSchedule(),
                1,
            ),
        )

        return settingsList
    }

    private fun expectedSchedule() = Schedule(
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        -1,
        -1,
        false,
    )

    private fun expectedAppearance() = Appearance(
        -1,
        "#FFFFFFFF",
        "Sans Serif",
        "Regular",
        true,
        true,
        16,
        "#FF000000",
        16,
        "#FF000000",
        false,
        16,
        "#FF000000",
        false,
        false,
        "#FF000000",
        false,
        true,
        true,
        false,
        false,
        true,
        true,
        false,
    )

    private fun expectedQuotations() = Quotations(
        true,
        true,
        "",
        false,
        -1,
        "",
        false,
        false,
        false,
        -1,
        "",
        true,
        false,
        false,
        "",
        "",
        "",
        true,
    )
}
