package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferBackupPreviousTest : DatabaseTestHelper() {
    @Test
    fun previous() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01()
            insertQuotationTestData02()

            setupTestData()

            val previousList = TransferBackupPrevious().previous(databaseRepositoryDouble)
            assertTrue(previousList.size == 4)
            assertEquals(previousList[0].contentType, 4)
            assertEquals(previousList[0].digest, "d3456789")
            assertEquals(previousList[0].widgetId, 13)
            assertEquals(previousList[0].db, "internal")
            assertEquals(previousList[1].contentType, 3)
            assertEquals(previousList[1].digest, "d1234567")
            assertEquals(previousList[1].widgetId, 12)
            assertEquals(previousList[1].db, "internal")
        }
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsPrevious(12, ContentSelection.ALL, "d4567890")
        databaseRepositoryDouble.markAsPrevious(13, ContentSelection.FAVOURITES, "d2345678")
        databaseRepositoryDouble.markAsPrevious(12, ContentSelection.AUTHOR, "d1234567")
        databaseRepositoryDouble.markAsPrevious(13, ContentSelection.SEARCH, "d3456789")
    }

    @Test
    fun currentInternalAndExternal() {
        if (canWorkWithMockk()) {
            populateInternal(14)
            populateExternal(14)

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(14)

            val previousList = TransferBackupPrevious().previous(databaseRepositoryDouble)

            assertTrue(previousList.size == 2)
            assertEquals(previousList[0].digest, "d1234567")
            assertEquals(previousList[0].db, "internal")
            assertEquals(previousList[1].digest, "00000000")
            assertEquals(previousList[1].db, "external")
        }
    }
}
