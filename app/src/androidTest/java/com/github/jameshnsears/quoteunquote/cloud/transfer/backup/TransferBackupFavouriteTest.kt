package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferBackupFavouriteTest : DatabaseTestHelper() {
    @Test
    fun favourite() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        setupTestData()

        val favouritesList = TransferBackupFavourite().favourite(databaseRepositoryDouble)
        assertTrue(favouritesList.size == 2)
        assertEquals(favouritesList[0].digest, "11111111")
        assertEquals(favouritesList[1].digest, "00000000")
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsFavourite("00000000")
        databaseRepositoryDouble.markAsFavourite("11111111")
    }
}
