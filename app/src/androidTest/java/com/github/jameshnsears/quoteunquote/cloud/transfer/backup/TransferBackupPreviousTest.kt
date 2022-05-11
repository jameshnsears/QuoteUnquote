package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.os.Build
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferBackupPreviousTest : DatabaseTestHelper() {
    @Test
    fun previous() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            insertQuotationTestData01()
            insertQuotationTestData02()

            setupTestData()

            val previousList = TransferBackupPrevious().previous(databaseRepositoryDouble)
            assertTrue(previousList.size == 4)
            assertEquals(previousList[0].contentType, 4)
            assertEquals(previousList[0].digest, "d3")
            assertEquals(previousList[0].widgetId, 13)
            assertEquals(previousList[1].contentType, 3)
            assertEquals(previousList[1].digest, "d1")
            assertEquals(previousList[1].widgetId, 12)
        }
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsPrevious(12, ContentSelection.ALL, "d4")
        databaseRepositoryDouble.markAsPrevious(13, ContentSelection.FAVOURITES, "d2")
        databaseRepositoryDouble.markAsPrevious(12, ContentSelection.AUTHOR, "d1")
        databaseRepositoryDouble.markAsPrevious(13, ContentSelection.SEARCH, "d3")
    }
}
