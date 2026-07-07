package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.db.DatabaseTestHelper
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import io.mockk.every
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TransferBackupPreviousTest : DatabaseTestHelper() {
    @Test
    fun previous() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01(true)
            insertQuotationTestData02(true)

            setupTestData()

            val previousList = TransferBackupPrevious().previous(databaseRepositoryDouble)
            assertThat(previousList.size, equalTo(4))
            assertThat(previousList[0].contentType, equalTo(4))
            assertThat(previousList[0].digest, equalTo("d3456789"))
            assertThat(previousList[0].widgetId, equalTo(13))
            assertThat(previousList[0].db, equalTo("internal"))
            assertThat(previousList[1].contentType, equalTo(3))
            assertThat(previousList[1].digest, equalTo("d1234567"))
            assertThat(previousList[1].widgetId, equalTo(12))
            assertThat(previousList[1].db, equalTo("internal"))
        }
    }

    fun setupTestData() {
        databaseRepositoryDouble.markAsPrevious(true, 12, ContentSelection.ALL, "d4567890")
        databaseRepositoryDouble.markAsPrevious(true, 13, ContentSelection.FAVOURITES, "d2345678")
        databaseRepositoryDouble.markAsPrevious(true, 12, ContentSelection.AUTHOR, "d1234567")
        databaseRepositoryDouble.markAsPrevious(true, 13, ContentSelection.SEARCH, "d3456789")
    }

    @Test
    fun currentInternalAndExternal() {
        if (canWorkWithMockk()) {
            populateInternal(14)
            populateExternal(14)

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(14)

            val previousList = TransferBackupPrevious().previous(databaseRepositoryDouble)

            assertThat(previousList.size, equalTo(2))
            assertThat(previousList[0].digest, equalTo("d1234567"))
            assertThat(previousList[0].db, equalTo("internal"))
            assertThat(previousList[1].digest, equalTo("00000000"))
            assertThat(previousList[1].db, equalTo("external"))
        }
    }
}
