package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer;
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferRestoreResponse;
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;

import timber.log.Timber;

public class CloudServiceRestore extends CloudService {
    @Override
    public int onStartCommand(
            @NonNull final Intent intent,
            final int flags,
            final int startId) {

        if (!CloudService.isRunning) {
            CloudService.isRunning = true;

            new Thread(() -> {
                Timber.d("isRunning=%b", CloudService.isRunning);

                final Context context = getServiceContext();

                if (!cloudTransfer.isInternetAvailable(context)) {
                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_archive_internet_missing),
                            Toast.LENGTH_SHORT).show());
                } else {
                    int widgetId = intent.getIntExtra("widgetId", 0);

                    SyncPreferences syncPreferences = new SyncPreferences(widgetId, context);

                    syncPreferences.setArchiveGoogleCloud(syncPreferences.getArchiveGoogleCloud());
                    syncPreferences.setArchiveSharedStorage(syncPreferences.getArchiveSharedStorage());

                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_archive_restore_receiving),
                            Toast.LENGTH_SHORT).show());

                    TransferRestoreResponse transferRestoreResponse
                            = cloudTransfer.restore(
                            CloudTransfer.TIMEOUT_SECONDS,
                            new TransferRestore().requestJson(
                                    intent.getStringExtra("remoteCodeValue")));

                    if (transferRestoreResponse.getReason().equals("no JSON for code")) {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_restore_missing_code),
                                Toast.LENGTH_SHORT).show());
                    } else if (transferRestoreResponse == null) {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_internet_missing),
                                Toast.LENGTH_SHORT).show());
                    } else {
                        TransferRestore transferRestore = new TransferRestore();
                        Transfer transfer = transferRestoreResponse.getTransfer();

                        DatabaseRepository databaseRepository
                                = DatabaseRepository.getInstance(context);

                        transferRestore.restore(context, databaseRepository, transfer);

                        databaseRepository.alignHistoryWithQuotations(widgetId, context);

                        DatabaseRepository.useInternalDatabase = true;

                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_restore_success),
                                Toast.LENGTH_SHORT).show());
                    }
                }

                broadcastEvent(SyncFragment.CLOUD_SERVICE_COMPLETED);

                CloudService.isRunning = false;
                Timber.d("isRunning=%b", CloudService.isRunning);

                stopSelf();

            }).start();
        }

        return Service.START_NOT_STICKY;
    }
}
