package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.GsonTestHelper
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TransferBackupCurrentTest : GsonTestHelper() {
    @Test
    fun current() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

    fun expectedCurrent(): List<Current> {
        setupTestData()

        val currentList = mutableListOf<Current>()
        currentList.add(
            Current("7a36e553", 12)
        )
        currentList.add(
            Current("7a36e553", 13)
        )

        return currentList
    }
}
