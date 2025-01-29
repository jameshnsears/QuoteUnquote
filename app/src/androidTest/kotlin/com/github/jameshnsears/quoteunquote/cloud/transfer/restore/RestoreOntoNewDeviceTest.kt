package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import org.junit.Assert.assertEquals
import org.junit.Test

class RestoreOntoNewDeviceTest : TransferRestoreUtility() {
    @Test
    fun restore_issue_405() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1))
            markDefaultQuotationAsFavourite()

            // restore
            val restoreTransfer = getTransferAsset("restore_issue_405.json")
            assertEquals(6, restoreTransfer.current.size)
            assertEquals(242, restoreTransfer.favourites.size)
            assertEquals(5616, restoreTransfer.previous.size)
            assertEquals(6, restoreTransfer.settings.size)

            val transferRestore = TransferRestore()
            val singleWidgetIdRestore = transferRestore.newDeviceTransferTransformer(
                restoreTransfer,
            )

            assertEquals(1, singleWidgetIdRestore.current.size)
            assertEquals(242, singleWidgetIdRestore.favourites.size)

            assertEquals(936, singleWidgetIdRestore.previous.size)
            assertEquals("a6e84457", singleWidgetIdRestore.previous[0].digest)
            assertEquals("81cc5827", singleWidgetIdRestore.previous[1].digest)
            assertEquals("86957672", singleWidgetIdRestore.previous[2].digest)
            assertEquals("7e0a6134", singleWidgetIdRestore.previous[3].digest)
            assertEquals("5b6037fd", singleWidgetIdRestore.previous[4].digest)

            assertEquals(1, singleWidgetIdRestore.settings.size)

//            val restoreJson = TransferRestore().asJson(singleWidgetIdRestore)
        }
    }
}
