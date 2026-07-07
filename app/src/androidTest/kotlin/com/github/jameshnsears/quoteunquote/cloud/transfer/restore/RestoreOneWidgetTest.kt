package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class RestoreOneWidgetTest : TransferRestoreUtility() {
    @Test
    fun restoreOneIntoOne() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1))
            markDefaultQuotationAsFavourite(true)

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertThat(backupTransfer.code, not(equalTo(getLocalCode())))
            assertThat(backupTransfer.code, equalTo(restoreTransfer.code))

            assertThat(backupTransfer.current.size, equalTo(1))
            assertThat(backupTransfer.favourites.size, equalTo(0))
            assertThat(backupTransfer.previous.size, equalTo(2))

            assertThat(backupTransfer.settings.size, equalTo(1))
            assertThat(
                backupTransfer.settings[0].appearance,
                equalTo(restoreTransfer.settings[0].appearance),
            )
            // a restore from a version that had no internal/external database, into one that does
            assertThat(
                backupTransfer.settings[0].quotations,
                not(equalTo(restoreTransfer.settings[0].quotations)),
            )
            assertThat(
                backupTransfer.settings[0].quotations.databaseInternal,
                not(equalTo(restoreTransfer.settings[0].quotations.databaseInternal)),
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                assertThat(
                    backupTransfer.settings[0].schedule,
                    equalTo(restoreTransfer.settings[0].schedule),
                )
            }
        }
    }

    @Test
    fun restoreOneIntoTwo() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1, 2))

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertThat(backupTransfer.current.size, equalTo(2))
            assertThat(backupTransfer.favourites.isEmpty(), `is`(true))
            assertThat(backupTransfer.previous.size, equalTo(4))

            assertThat(backupTransfer.settings.size, equalTo(2))

            assertThat(
                backupTransfer.settings[0].appearance,
                equalTo(restoreTransfer.settings[0].appearance),
            )
            assertThat(
                backupTransfer.settings[1].appearance,
                equalTo(backupTransfer.settings[0].appearance),
            )

            assertThat(
                backupTransfer.settings[0].quotations,
                not(equalTo(restoreTransfer.settings[0].quotations)),
            )
            assertThat(
                backupTransfer.settings[0].quotations.databaseInternal,
                not(equalTo(restoreTransfer.settings[0].quotations.databaseInternal)),
            )
            assertThat(
                backupTransfer.settings[1].quotations,
                equalTo(backupTransfer.settings[0].quotations),
            )
        }
    }
}
