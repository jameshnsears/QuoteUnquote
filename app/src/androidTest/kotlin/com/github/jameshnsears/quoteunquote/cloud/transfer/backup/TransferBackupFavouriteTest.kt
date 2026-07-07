package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.db.DatabaseTestHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TransferBackupFavouriteTest : DatabaseTestHelper() {
    @Test
    fun favouriteInternal() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)

        setupTestData()

        val favouritesList = TransferBackupFavourite().favourite(databaseRepositoryDouble)

        assertThat(favouritesList.size, equalTo(2))
        assertThat(favouritesList[0].digest, equalTo("d2345678"))
        assertThat(favouritesList[0].db, equalTo("internal"))
        assertThat(favouritesList[1].digest, equalTo("d1234567"))
        assertThat(favouritesList[1].db, equalTo("internal"))
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsFavourite(true, "d1234567")
        databaseRepositoryDouble.markAsFavourite(true, "d2345678")
    }

    @Test
    fun favouriteInternalAndExternal() {
        populateInternal(10)
        populateExternal(10)

        val favouritesList = TransferBackupFavourite().favourite(databaseRepositoryDouble)

        assertThat(favouritesList.size, equalTo(2))
        assertThat(favouritesList[0].digest, equalTo("d1234567"))
        assertThat(favouritesList[0].db, equalTo("internal"))
        assertThat(favouritesList[1].digest, equalTo("00000000"))
        assertThat(favouritesList[1].db, equalTo("external"))
    }
}
