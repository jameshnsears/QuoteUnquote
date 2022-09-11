package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TransferBackupTest : DatabaseTestHelper() {
    @Test
    fun transfer() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01()
            insertQuotationTestData02()

            val (backupTransfer, transferCode) = getBackupTransfer()

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(12, 13)

            TransferBackupCurrentTest().setupTestData()
            TransferBackupPreviousTest().setupTestData()
            TransferBackupFavouriteTest().setupTestData()

            val transfer = backupTransfer.transfer(databaseRepositoryDouble)
            // val transferAsJson = GsonTestHelper().gson().toJson(transfer)

            assertEquals(transfer.code, transferCode)

            assertNotNull(transfer.settings)
            assertEquals(transfer.settings.size, 2)
            assertNotNull(transfer.current)
            assertNotNull(transfer.previous)
            assertNotNull(transfer.favourites)
        }
    }

    @Test
    fun transferInternalAndExternal() {
        if (canWorkWithMockk()) {
            populateExternal(50)
            populateInternal(50)

            val (backupTransfer, _) = getBackupTransfer()

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(50)

            val transfer = backupTransfer.transfer(databaseRepositoryDouble)
//            val transferAsJson = GsonTestHelper().gson().toJson(transfer)

            assertNotNull(transfer.settings)
            assertEquals(transfer.settings.size, 1)
            assertEquals(2, transfer.current.size)
            assertEquals(2, transfer.previous.size)
            assertEquals(2, transfer.favourites.size)
        }
    }

    private fun getBackupTransfer(): Pair<TransferBackup, String> {
        val backupTransfer = spyk(TransferBackup(context))
        val transferCode = "012345672e"
        every { backupTransfer.getLocalCode() } returns transferCode

        return Pair(backupTransfer, transferCode)
    }
}
