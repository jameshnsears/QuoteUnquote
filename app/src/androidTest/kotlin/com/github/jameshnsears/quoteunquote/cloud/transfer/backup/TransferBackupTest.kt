package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.db.DatabaseTestHelper
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

class TransferBackupTest : DatabaseTestHelper() {
    @Test
    fun transfer() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01(true)
            insertQuotationTestData02(true)

            val (backupTransfer, transferCode) = getBackupTransfer()

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(12, 13)

            TransferBackupCurrentTest().setupTestData()
            TransferBackupPreviousTest().setupTestData()
            TransferBackupFavouriteTest().setupTestData()

            val transfer = backupTransfer.transfer(databaseRepositoryDouble)
            // val transferAsJson = GsonTestHelper().gson().toJson(transfer)

            assertThat(transfer.code, equalTo(transferCode))

            assertThat(transfer.settings, notNullValue())
            assertThat(transfer.settings.size, equalTo(2))
            assertThat(transfer.current, notNullValue())
            assertThat(transfer.previous, notNullValue())
            assertThat(transfer.favourites, notNullValue())
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

            assertThat(transfer.settings, notNullValue())
            assertThat(transfer.settings.size, equalTo(1))
            assertThat(transfer.current.size, equalTo(2))
            assertThat(transfer.previous.size, equalTo(2))
            assertThat(transfer.favourites.size, equalTo(2))
        }
    }

    private fun getBackupTransfer(): Pair<TransferBackup, String> {
        val backupTransfer = spyk(TransferBackup(context))
        val transferCode = "012345672e"
        every { backupTransfer.getLocalCode() } returns transferCode

        return Pair(backupTransfer, transferCode)
    }
}
