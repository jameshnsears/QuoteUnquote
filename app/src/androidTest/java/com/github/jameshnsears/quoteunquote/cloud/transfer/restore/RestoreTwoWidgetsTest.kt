package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Test

class RestoreTwoWidgetsTest : TransferRestoreUtility() {
    @Test
    fun restoreTwoWithFavouritesIntoOne() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            setupWidgets(intArrayOf(1))

            // restore
            val restoreTransfer = getTransferAsset("restore_two_widgets_with_favourites.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)
            val backupJson = TransferBackup(context).asJson(backupTransfer)

            Assert.assertEquals(getLocalCode(), backupTransfer.code)

            Assert.assertTrue(backupTransfer.current.size == 1)
            Assert.assertTrue(backupTransfer.favourites.size == 4)
            Assert.assertTrue(backupTransfer.previous.size == 5)

            Assert.assertTrue(backupTransfer.settings.size == 1)
            Assert.assertEquals(
                restoreTransfer.settings[0].appearance,
                backupTransfer.settings[0].appearance,
            )
            Assert.assertEquals(
                restoreTransfer.settings[0].quotations,
                backupTransfer.settings[0].quotations,
            )
            Assert.assertEquals(
                restoreTransfer.settings[0].schedule,
                backupTransfer.settings[0].schedule,
            )
        }
    }

    @Test
    fun restoreTwoWithFavouritesIntoTwo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            setupWidgets(intArrayOf(1, 2))

            // restore
            val restoreTransfer = getTransferAsset("restore_two_widgets_with_favourites.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)
            val backupJson = TransferBackup(context).asJson(backupTransfer)

            Assert.assertEquals(getLocalCode(), backupTransfer.code)

            Assert.assertTrue(backupTransfer.current.size == 2)
            Assert.assertTrue(backupTransfer.favourites.size == 4)
            Assert.assertTrue(backupTransfer.previous.size == 10)

            Assert.assertTrue(backupTransfer.settings.size == 2)
            Assert.assertEquals(
                restoreTransfer.settings[0].appearance,
                backupTransfer.settings[0].appearance,
            )
            Assert.assertEquals(
                restoreTransfer.settings[0].quotations,
                backupTransfer.settings[0].quotations,
            )
            Assert.assertEquals(
                restoreTransfer.settings[0].schedule,
                backupTransfer.settings[0].schedule,
            )

            Assert.assertEquals(
                restoreTransfer.settings[1].appearance,
                backupTransfer.settings[1].appearance,
            )
            Assert.assertEquals(
                restoreTransfer.settings[1].quotations,
                backupTransfer.settings[1].quotations,
            )
            Assert.assertEquals(
                restoreTransfer.settings[1].schedule,
                backupTransfer.settings[1].schedule,
            )
        }
    }

    @Test
    fun restoreTwoIntoOne() {
        // defect raised by user!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // set up
            val restoreTransfer = getTransferAsset("restore_order_test.json")
            setUp(restoreTransfer)

            // restore
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // asserts
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)
            val backupJson = TransferBackup(context).asJson(backupTransfer)

            Assert.assertEquals(getLocalCode(), backupTransfer.code)

            Assert.assertTrue(backupTransfer.current.size == 1)
            Assert.assertEquals(restoreTransfer.current[0].digest, backupTransfer.current[0].digest)

            Assert.assertTrue(backupTransfer.favourites.size == 8)

            // the restore contains two widgets; with a duplicate previous value, each with
            // a different widgetId; we are restoring into only one widget
            Assert.assertTrue(restoreTransfer.previous.size == 53)
            Assert.assertTrue(backupTransfer.previous.size == 52)
        }
    }

    private fun setUp(restoreTransfer: Transfer) {
        createSharedPreferencesWithLocalCode(getLocalCode())
        mockkObject(TransferUtility)
        every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

        // insert all digests in the .json into the database
        val digestsFromTransfer: MutableList<String> = ArrayList()

        for (index in (restoreTransfer.current.size - 1) downTo 0 step 1) {
            digestsFromTransfer.add(restoreTransfer.current[index].digest)
        }

        for (index in (restoreTransfer.favourites.size - 1) downTo 0 step 1) {
            digestsFromTransfer.add(restoreTransfer.favourites[index].digest)
        }

        for (index in (restoreTransfer.previous.size - 1) downTo 0 step 1) {
            digestsFromTransfer.add(restoreTransfer.previous[index].digest)
        }

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        for (digest in digestsFromTransfer.distinct()) {
            quotationEntityList.add(
                QuotationEntity(
                    digest,
                    "w-$digest",
                    "a-$digest",
                    "q-$digest"
                )
            )
        }
        databaseRepositoryDouble.insertQuotations(quotationEntityList.distinct())
    }
}
