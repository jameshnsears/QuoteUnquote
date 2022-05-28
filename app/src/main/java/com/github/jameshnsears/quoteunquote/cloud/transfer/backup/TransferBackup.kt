package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferCommon
import com.github.jameshnsears.quoteunquote.configure.fragment.archive.ArchivePreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

open class TransferBackup(val context: Context) : TransferCommon() {
    fun transfer(databaseRepository: DatabaseRepository): Transfer {
        return Transfer(
            getLocalCode(),
            TransferBackupCurrent(context).current(databaseRepository),
            TransferBackupFavourite().favourite(databaseRepository),
            TransferBackupPrevious().previous(databaseRepository),
            TransferBackupSettings(context).settings()
        )
    }

    open fun getLocalCode(): String {
        return ArchivePreferences(
            0,
            context
        ).transferLocalCode
    }
}
