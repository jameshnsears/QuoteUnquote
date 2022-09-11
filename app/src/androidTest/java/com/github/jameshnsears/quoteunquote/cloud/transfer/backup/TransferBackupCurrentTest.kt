package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.GsonTestHelper
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferBackupCurrentTest : GsonTestHelper() {
    @Test
    fun current() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01()
            insertQuotationTestData02()

            setupTestData()

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(12, 13)

            assertEquals(
                gson().toJson(expectedCurrent()),
                gson().toJson(TransferBackupCurrent(context).current(databaseRepositoryDouble))
            )
        }
    }

    fun setupTestData() {
        setDefaultQuotationAll(12)
        setDefaultQuotationAll(13)
    }

    private fun expectedCurrent(): List<Current> {
        setupTestData()

        val currentList = mutableListOf<Current>()
        currentList.add(
            Current(DatabaseRepository.getDefaultQuotationDigest(), 12, "internal")
        )
        currentList.add(
            Current(DatabaseRepository.getDefaultQuotationDigest(), 13, "internal")
        )

        return currentList
    }

    @Test
    fun currentInternalAndExternal() {
        if (canWorkWithMockk()) {
            populateInternal(14)
            populateExternal(14)

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(14)

            val currentList = TransferBackupCurrent(context).current(databaseRepositoryDouble)

            assertTrue(currentList.size == 2)
            assertEquals(currentList[0].digest, "d1234567")
            assertEquals(currentList[0].db, "internal")
            assertEquals(currentList[1].digest, "00000000")
            assertEquals(currentList[1].db, "external")
        }
    }
}
