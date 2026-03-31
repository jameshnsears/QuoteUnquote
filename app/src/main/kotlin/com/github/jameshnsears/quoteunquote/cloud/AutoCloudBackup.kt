package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import timber.log.Timber
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AutoCloudBackup(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_TAG = "AutoCloudBackup"
        const val KEY_WIDGET_ID = "widgetId"
    }

    override suspend fun doWork(): Result {
        Timber.d("$WORK_TAG.start")

        return try {
            val cloudTransfer = CloudTransfer()

            if (!cloudTransfer.isInternetAvailable(applicationContext)) {
                Timber.w("$WORK_TAG.retry: No internet connection")
                return Result.retry()
            }

            val quoteUnquoteModel = QuoteUnquoteModel(-1, applicationContext)
            val backupData = quoteUnquoteModel.transferBackup(applicationContext)

            if (!cloudTransfer.backup(backupData)) {
                throw IOException("Cloud backup operation failed")
            }

            updateSyncPreferences()

            Timber.d("$WORK_TAG.success")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "$WORK_TAG.failure: ${e.message}")
            Result.retry()
        }
    }

    private fun updateSyncPreferences() {
        val widgetId = inputData.getInt(KEY_WIDGET_ID, 0)
        val syncPreferences = SyncPreferences(widgetId, applicationContext)

        val currentTimestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE, HH:mm:ss"))

        syncPreferences.lastSuccessfulCloudBackupTimestamp = currentTimestamp
        Timber.d("$WORK_TAG.timestampUpdated: $currentTimestamp for widget $widgetId")
    }
}
