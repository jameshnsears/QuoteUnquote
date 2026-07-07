package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class RestoreOntoNewDeviceTest : TransferRestoreUtility() {
    @Test
    fun restore_issue_405() {
        if (canWorkWithMockk()) {
            // set up
            setupWidgets(intArrayOf(1))
            markDefaultQuotationAsFavourite(true)

            // restore
            val restoreTransfer = getTransferAsset("restore_issue_405.json")
            assertThat(restoreTransfer.current.size, equalTo(6))
            assertThat(restoreTransfer.favourites.size, equalTo(242))
            assertThat(restoreTransfer.previous.size, equalTo(5616))
            assertThat(restoreTransfer.settings.size, equalTo(6))

            val transferRestore = TransferRestore()
            val singleWidgetIdRestore =
                transferRestore.newDeviceTransferTransformer(
                    restoreTransfer,
                )

            assertThat(singleWidgetIdRestore.current.size, equalTo(1))
            assertThat(singleWidgetIdRestore.favourites.size, equalTo(242))

            assertThat(singleWidgetIdRestore.previous.size, equalTo(936))
            assertThat(singleWidgetIdRestore.previous[0].digest, equalTo("a6e84457"))
            assertThat(singleWidgetIdRestore.previous[1].digest, equalTo("81cc5827"))
            assertThat(singleWidgetIdRestore.previous[2].digest, equalTo("86957672"))
            assertThat(singleWidgetIdRestore.previous[3].digest, equalTo("7e0a6134"))
            assertThat(singleWidgetIdRestore.previous[4].digest, equalTo("5b6037fd"))

            assertThat(singleWidgetIdRestore.settings.size, equalTo(1))

//            val restoreJson = TransferRestore().asJson(singleWidgetIdRestore)
        }
    }
}
