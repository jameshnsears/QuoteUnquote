package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.GsonTestHelper
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import io.mockk.every
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TransferBackupCurrentTest : GsonTestHelper() {
    @Test
    fun current() {
        if (canWorkWithMockk()) {
            insertQuotationTestData01(true)
            insertQuotationTestData02(true)

            setupTestData()

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(12, 13)

            assertThat(
                gson().toJson(TransferBackupCurrent(context).current(databaseRepositoryDouble)),
                equalTo(gson().toJson(expectedCurrent())),
            )
        }
    }

    fun setupTestData() {
        setDefaultQuotationAll(true, 12)
        setDefaultQuotationAll(true, 13)
    }

    private fun expectedCurrent(): List<Current> {
        setupTestData()

        val currentList = mutableListOf<Current>()
        currentList.add(
            Current(DatabaseRepository.getDefaultQuotationDigest(true), 12, "internal"),
        )
        currentList.add(
            Current(DatabaseRepository.getDefaultQuotationDigest(true), 13, "internal"),
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

            assertThat(currentList.size, equalTo(2))
            assertThat(currentList[0].digest, equalTo("d1234567"))
            assertThat(currentList[0].db, equalTo("internal"))
            assertThat(currentList[1].digest, equalTo("00000000"))
            assertThat(currentList[1].db, equalTo("external"))
        }
    }
}
