package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import io.mockk.every
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class RestoreTwoWidgetsTest : TransferRestoreUtility() {
    @Test
    fun restoreTwoWithFavouritesIntoOne() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1))

            // restore
            val restoreTransfer = getTransferAsset("restore_two_widgets_with_favourites.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertThat(backupTransfer.code, not(equalTo(getLocalCode())))
            assertThat(backupTransfer.code, equalTo(restoreTransfer.code))

            assertThat(backupTransfer.current.size, equalTo(1))
            assertThat(backupTransfer.favourites.size, equalTo(4))
            assertThat(backupTransfer.previous.size, equalTo(5))

            assertThat(backupTransfer.settings.size, equalTo(1))
            assertThat(
                backupTransfer.settings[0].appearance,
                equalTo(restoreTransfer.settings[0].appearance),
            )

            assertThat(
                backupTransfer.settings[0].quotations,
                not(equalTo(restoreTransfer.settings[0].quotations)),
            )
            assertThat(
                backupTransfer.settings[0].quotations.databaseInternal,
                not(equalTo(restoreTransfer.settings[0].quotations.databaseInternal)),
            )

            assertThat(
                backupTransfer.settings[0].schedule,
                equalTo(restoreTransfer.settings[0].schedule),
            )
        }
    }

    @Test
    fun restoreTwoWithFavouritesIntoTwo() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1, 2))

            // restore
            val restoreTransfer = getTransferAsset("restore_two_widgets_with_favourites.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertThat(backupTransfer.code, not(equalTo(getLocalCode())))
            assertThat(backupTransfer.code, equalTo(restoreTransfer.code))

            assertThat(backupTransfer.current.size, equalTo(2))
            assertThat(backupTransfer.favourites.size, equalTo(4))
            assertThat(backupTransfer.previous.size, equalTo(10))

            assertThat(backupTransfer.settings.size, equalTo(2))
            assertThat(
                backupTransfer.settings[0].appearance,
                equalTo(restoreTransfer.settings[0].appearance),
            )

            assertThat(
                backupTransfer.settings[0].quotations,
                not(equalTo(restoreTransfer.settings[0].quotations)),
            )
            assertThat(
                backupTransfer.settings[0].quotations.databaseInternal,
                not(equalTo(restoreTransfer.settings[0].quotations.databaseInternal)),
            )

            assertThat(
                backupTransfer.settings[0].schedule,
                equalTo(restoreTransfer.settings[0].schedule),
            )

            assertThat(
                backupTransfer.settings[1].appearance,
                equalTo(restoreTransfer.settings[1].appearance),
            )

            assertThat(
                backupTransfer.settings[1].schedule,
                equalTo(restoreTransfer.settings[1].schedule),
            )
        }
    }

    @Test
    fun restoreTwoIntoOne() {
        // defect raised by user!
        if (canWorkWithMockk()) {
            // set up
            val restoreTransfer = getTransferAsset("restore_order_test.json")
            setUp(restoreTransfer)

            // restore
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // asserts
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
//            val restoreJson = TransferRestore().asJson(restoreTransfer)
//            val backupJson = TransferBackup(context).asJson(backupTransfer)

            assertThat(backupTransfer.code, not(equalTo(getLocalCode())))
            assertThat(backupTransfer.code, equalTo(restoreTransfer.code))

            assertThat(backupTransfer.current.size, equalTo(1))
            assertThat(backupTransfer.current[0].digest, equalTo(restoreTransfer.current[0].digest))

            assertThat(backupTransfer.favourites.size, equalTo(8))

            // the restore contains two widgets; with a duplicate previous value, each with
            // a different widgetId; we are restoring into only one widget
            assertThat(restoreTransfer.previous.size, equalTo(53))
            assertThat(backupTransfer.previous.size, equalTo(52))
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
                    "q-$digest",
                ),
            )
        }
        databaseRepositoryDouble.insertQuotations(
            true,
            quotationEntityList.distinct(),
        )
    }
}
