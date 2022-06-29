package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

open class TransferBackupCurrent(val context: Context) {
    fun current(databaseRepository: DatabaseRepository): List<Current> {
        val originalUseInternalDatabaseState = DatabaseRepository.useInternalDatabase

        val internalDatabaseCurrent =
            getCurrentFromDatabase(
                TransferUtility.getWidgetIds(context),
                databaseRepository,
                true
            )
        val externalDatabaseCurrent =
            getCurrentFromDatabase(
                TransferUtility.getWidgetIds(context),
                databaseRepository,
                false
            )

        DatabaseRepository.useInternalDatabase = originalUseInternalDatabaseState

        return internalDatabaseCurrent + externalDatabaseCurrent
    }

    private fun getCurrentFromDatabase(
        widgetIds: IntArray,
        databaseRepository: DatabaseRepository,
        useInternalDatabase: Boolean
    ): List<Current> {
        DatabaseRepository.useInternalDatabase = useInternalDatabase

        val currentList = mutableListOf<Current>()

        for (widgetIdsIndex in widgetIds.indices) {
            val widgetId = widgetIds[widgetIdsIndex]

            if (databaseRepository.getCurrentQuotation(widgetId) != null) {
                currentList.add(
                    Current(
                        databaseRepository.getCurrentQuotation(widgetId).digest,
                        widgetId,
                        if (useInternalDatabase) {
                            "internal"
                        } else {
                            "external"
                        }
                    )
                )
            }
        }

        return currentList
    }
}
