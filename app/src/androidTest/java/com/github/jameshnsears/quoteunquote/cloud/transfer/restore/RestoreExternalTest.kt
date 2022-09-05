package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreExternalTest : TransferRestoreUtility() {
    @Test
    fun restoreWhereExternalBeenPopulated() {
        if (canWorkWithMockk()) {
            // set up
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

            createSharedPreferencesWithLocalCode(getLocalCode())

            insertInternalQuotations()
            insertExternalQuotations()

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget_with_external.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertTrue(backupTransfer.current.size == 2)
            assertTrue(backupTransfer.favourites.size == 1)
            assertTrue(backupTransfer.previous.size == 2)
        }
    }

    @Test
    fun restoreWhereExternalNotBeenPopulated() {
        if (canWorkWithMockk()) {
            // set up
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(2)

            createSharedPreferencesWithLocalCode(getLocalCode())

            insertInternalQuotations()

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget_with_external.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertTrue(backupTransfer.current.size == 1)
            assertTrue(backupTransfer.favourites.isEmpty())
            assertTrue(backupTransfer.previous.size == 1)
        }
    }

    @Test
    fun ensureDatabaseRestoreConsistency() {
        insertInternalQuotations()
        insertExternalQuotations()

        val restoreTransfer = getTransferAsset("restoreAgainstMissingExternal.json")

        val transferRestore = TransferRestore()

//        val restoreJson = TransferRestore()
//            .asJson(restoreTransfer)
//        val backupJson = TransferBackup(context)
//            .asJson(TransferBackup(context).transfer(databaseRepositoryDouble))

        assertFalse(
            transferRestore.testRestoreForDatabaseConsistency(
                databaseRepositoryDouble,
                restoreTransfer
            ).get()
        )
    }
}
