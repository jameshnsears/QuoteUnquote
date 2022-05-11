package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

open class TransferBackupPrevious {
    fun previous(databaseRepository: DatabaseRepository): List<Previous> {
        val previousList = mutableListOf<Previous>()

        for (previous in databaseRepository.previous) {
            previousList.add(
                Previous(
                    previous.contentType.contentSelection,
                    previous.digest,
                    previous.navigation,
                    previous.widgetId
                )
            )
        }

        return previousList
    }
}
