package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

open class TransferBackupPrevious {
    fun previous(databaseRepository: DatabaseRepository): List<Previous> {
        val originalUseInternalDatabaseState = DatabaseRepository.useInternalDatabase

        val internalDatabasePrevious = getPreviousFromDatabase(databaseRepository, true)
        val externalDatabasePrevious = getPreviousFromDatabase(databaseRepository, false)

        DatabaseRepository.useInternalDatabase = originalUseInternalDatabaseState

        return internalDatabasePrevious + externalDatabasePrevious
    }

    private fun getPreviousFromDatabase(
        databaseRepository: DatabaseRepository,
        useInternalDatabase: Boolean,
    ): List<Previous> {
        DatabaseRepository.useInternalDatabase = useInternalDatabase

        val previousList = mutableListOf<Previous>()

        for (previous in databaseRepository.previous) {
            if (previous.widgetId != 0) {
                previousList.add(
                    Previous(
                        previous.contentType.contentSelection,
                        previous.digest,
                        previous.navigation,
                        previous.widgetId,
                        if (useInternalDatabase) {
                            "internal"
                        } else {
                            "external"
                        },
                    ),
                )
            }
        }

        return previousList
    }
}
