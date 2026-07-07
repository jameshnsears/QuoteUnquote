package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import io.mockk.every
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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

            assertThat(backupTransfer.current.size, equalTo(2))
            assertThat(backupTransfer.favourites.size, equalTo(1))
            assertThat(backupTransfer.previous.size, equalTo(2))
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

            assertThat(backupTransfer.current.size, equalTo(1))
            assertThat(backupTransfer.favourites.isEmpty(), `is`(true))
            assertThat(backupTransfer.previous.size, equalTo(1))
        }
    }
}
