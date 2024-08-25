package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class AutoCloudBackup(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    companion object {
        const val WORK_TAG = "AutoCloudBackup"
    }

    override fun doWork(): Result {
        Timber.d("autoBackupGoogleCloud.start")

        try {
            val cloudTransfer = CloudTransfer()

            if (!cloudTransfer.isInternetAvailable(context)) {
                throw IOException("isInternetAvailable")
            }

            val quoteUnquoteModel = QuoteUnquoteModel(-1, context)
            if (!cloudTransfer.backup(quoteUnquoteModel.transferBackup(context))) {
                throw IOException("backup")
            }

            val widgetId = inputData.getInt("widgetId", 0)
            val syncPreferences = SyncPreferences(widgetId, context)
            val formatter = SimpleDateFormat("EEEE, HH:mm:ss")
            val now = Date()
            val formattedDate = formatter.format(now)
            Timber.d("autoBackupGoogleCloud.formattedDate=$formattedDate")
            syncPreferences.lastSuccessfulCloudBackupTimestamp = formattedDate

            Timber.d("autoBackupGoogleCloud.end")
            return Result.success()
        } catch (e: IOException) {
            Timber.e("autoBackupGoogleCloud.retry: ${e.message}")
            Thread.sleep(60_000L)
            return Result.retry()
        }
    }
}
