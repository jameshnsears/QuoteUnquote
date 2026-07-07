package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.cloud.transfer.Appearance
import com.github.jameshnsears.quoteunquote.cloud.transfer.GsonTestHelper
import com.github.jameshnsears.quoteunquote.cloud.transfer.Quotations
import com.github.jameshnsears.quoteunquote.cloud.transfer.Schedule
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.Sync
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import io.mockk.every
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TransferBackupSettingsTest : GsonTestHelper() {
    @Test
    fun settingsQuotations() {
        assertThat(
            gson().toJson(
                TransferBackupSettings(context).settingsQuotations(
                    1,
                    getApplicationContext(),
                ),
            ),
            equalTo(gson().toJson(expectedQuotations())),
        )
    }

    @Test
    fun settingsAppearance() {
        assertThat(
            gson().toJson(
                TransferBackupSettings(context).settingsAppearance(
                    1,
                    getApplicationContext(),
                ),
            ),
            equalTo(gson().toJson(expectedAppearance())),
        )
    }

    @Test
    fun settingsSchedule() {
        assertThat(
            gson().toJson(
                TransferBackupSettings(context).settingsSchedule(
                    1,
                    getApplicationContext(),
                ),
            ),
            equalTo(gson().toJson(expectedSchedule())),
        )
    }

    @Test
    fun settings() {
        if (canWorkWithMockk()) {
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

            assertThat(
                gson().toJson(TransferBackupSettings(context).settings()),
                equalTo(gson().toJson(expectedSettings())),
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
                expectedSync(),
                1,
            ),
        )

        return settingsList
    }

    private fun expectedSync() =
        Sync(
            false,
        )

    private fun expectedSchedule() =
        Schedule(
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
            0,
            23,
            1,
            false,
            false,
        )

    private fun expectedAppearance() =
        Appearance(
            -1,
            "#FFFFFFFF",
            "Sans Serif",
            "Italic",
            false,
            true,
            false,
            18,
            "#FF000000",
            18,
            "#FF000000",
            false,
            18,
            "#FF000000",
            true,
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
            0,
        )

    private fun expectedQuotations() =
        Quotations(
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
