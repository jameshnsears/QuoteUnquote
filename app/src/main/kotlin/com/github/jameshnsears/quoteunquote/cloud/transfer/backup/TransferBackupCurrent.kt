package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository

open class TransferBackupCurrent(
    val context: Context,
) {
    fun current(databaseRepository: DatabaseRepository): List<Current> {
        val internalDatabaseCurrent =
            getCurrentFromDatabase(
                TransferUtility.getWidgetIds(context),
                databaseRepository,
                true,
            )
        val externalDatabaseCurrent =
            getCurrentFromDatabase(
                TransferUtility.getWidgetIds(context),
                databaseRepository,
                false,
            )

        return internalDatabaseCurrent + externalDatabaseCurrent
    }

    private fun getCurrentFromDatabase(
        widgetIds: IntArray,
        databaseRepository: DatabaseRepository,
        useInternalDatabase: Boolean,
    ): List<Current> {
        val currentList = mutableListOf<Current>()

        for (widgetIdsIndex in widgetIds.indices) {
            val widgetId = widgetIds[widgetIdsIndex]

            val currentQuotation = databaseRepository.getCurrentQuotation(useInternalDatabase, widgetId)
            if (currentQuotation != null) {
                currentList.add(
                    Current(
                        currentQuotation.digest,
                        widgetId,
                        if (useInternalDatabase) {
                            "internal"
                        } else {
                            "external"
                        },
                    ),
                )
            }
        }

        return currentList
    }
}
