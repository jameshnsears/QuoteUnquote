package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferBackupFavouriteTest : DatabaseTestHelper() {
    @Test
    fun favouriteInternal() {
        insertQuotationTestData01()
        insertQuotationTestData02()

        setupTestData()

        val favouritesList = TransferBackupFavourite().favourite(databaseRepositoryDouble)

        assertTrue(favouritesList.size == 2)
        assertEquals(favouritesList[0].digest, "d2345678")
        assertEquals(favouritesList[0].db, "internal")
        assertEquals(favouritesList[1].digest, "d1234567")
        assertEquals(favouritesList[1].db, "internal")
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsFavourite("d1234567")
        databaseRepositoryDouble.markAsFavourite("d2345678")
    }

    @Test
    fun favouriteInternalAndExternal() {
        populateInternal(10)
        populateExternal(10)

        val favouritesList = TransferBackupFavourite().favourite(databaseRepositoryDouble)

        assertTrue(favouritesList.size == 2)
        assertEquals(favouritesList[0].digest, "d1234567")
        assertEquals(favouritesList[0].db, "internal")
        assertEquals(favouritesList[1].digest, "00000000")
        assertEquals(favouritesList[1].db, "external")
    }
}
