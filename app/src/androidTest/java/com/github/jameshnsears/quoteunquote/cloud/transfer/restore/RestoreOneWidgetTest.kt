package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreOneWidgetTest : TransferRestoreUtility() {
    @Test
    fun restoreOneIntoOne() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            setupWidgets(intArrayOf(1))
            markDefaultQuotationAsFavourite()

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertEquals(getLocalCode(), backupTransfer.code)

            assertTrue(backupTransfer.current.size == 1)
            assertTrue(backupTransfer.favourites.size == 1)
            assertTrue(backupTransfer.previous.size == 2)

            assertTrue(backupTransfer.settings.size == 1)
            assertEquals(
                restoreTransfer.settings[0].appearance,
                backupTransfer.settings[0].appearance,
            )
            assertEquals(
                restoreTransfer.settings[0].quotations,
                backupTransfer.settings[0].quotations,
            )
            assertEquals(
                restoreTransfer.settings[0].schedule,
                backupTransfer.settings[0].schedule,
            )
        }
    }

    @Test
    fun restoreOneIntoTwo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            setupWidgets(intArrayOf(1, 2))

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertTrue(backupTransfer.current.size == 2)
            assertTrue(backupTransfer.favourites.isEmpty())
            assertTrue(backupTransfer.previous.size == 4)

            assertTrue(backupTransfer.settings.size == 2)

            assertEquals(
                restoreTransfer.settings[0].appearance,
                backupTransfer.settings[0].appearance,
            )
            assertEquals(
                backupTransfer.settings[0].appearance,
                backupTransfer.settings[1].appearance,
            )

            assertEquals(
                restoreTransfer.settings[0].quotations,
                backupTransfer.settings[0].quotations,
            )
            assertEquals(
                backupTransfer.settings[0].quotations,
                backupTransfer.settings[1].quotations,
            )

            assertEquals(
                restoreTransfer.settings[0].schedule,
                backupTransfer.settings[0].schedule,
            )
            assertEquals(
                backupTransfer.settings[0].schedule,
                backupTransfer.settings[1].schedule,
            )
        }
    }
}
