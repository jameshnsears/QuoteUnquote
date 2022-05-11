package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

open class TransferBackupCurrent(val context: Context) {
    fun current(databaseRepository: DatabaseRepository): List<Current> {
        val widgetIds = TransferUtility.getWidgetIds(context)

        val currentList = mutableListOf<Current>()

        for (widgetIdsIndex in widgetIds.indices) {
            val widgetId = widgetIds[widgetIdsIndex]
            currentList.add(
                Current(
                    databaseRepository.getCurrentQuotation(widgetId).digest,
                    widgetId
                )
            )
        }

        return currentList
    }
}
