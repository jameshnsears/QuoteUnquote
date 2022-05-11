package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import org.junit.Assert
import org.junit.Test

class RestoreTwoWidgetsWithFavourites : TransferRestoreUtility() {
    @Test
    fun restoreIntoOneWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            setupWidgets(intArrayOf(1))

            // restore
            val restoreTransfer = getTransferAsset("restore_two_widgets_with_favourites.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)
            val backupJson = TransferBackup(context).asJson(backupTransfer)

            Assert.assertEquals(getLocalCode(), backupTransfer.code)

            Assert.assertTrue(backupTransfer.current.size == 1)
            Assert.assertTrue(backupTransfer.favourites.size == 4)
            Assert.assertTrue(backupTransfer.previous.size == 5)

            Assert.assertTrue(backupTransfer.settings.size == 1)
            Assert.assertEquals(
                restoreTransfer.settings[1].appearance,
                backupTransfer.settings[0].appearance,
            )
            Assert.assertEquals(
                restoreTransfer.settings[1].quotations,
                backupTransfer.settings[0].quotations,
            )
            Assert.assertEquals(
                restoreTransfer.settings[1].schedule,
                backupTransfer.settings[0].schedule,
            )
        }
    }
}
