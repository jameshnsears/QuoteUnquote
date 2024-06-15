package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncFragment;

import timber.log.Timber;

public class CloudServiceBackup extends CloudService {
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
                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_archive_backup_sending),
                            Toast.LENGTH_SHORT).show());

                    // "large" amounts of data can not be sent as an Intent extra
                    QuoteUnquoteModel quoteUnquoteModel = new QuoteUnquoteModel(-1, context);
                    if (cloudTransfer.backup(quoteUnquoteModel.transferBackup(context))) {

                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_backup_success),
                                Toast.LENGTH_SHORT).show());
                    } else {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_internet_missing),
                                Toast.LENGTH_SHORT).show());
                    }
                }

                CloudService.isRunning = false;
                Timber.d("isRunning=%b", CloudService.isRunning);

                this.broadcastEvent(SyncFragment.CLOUD_SERVICE_COMPLETED);

                stopSelf();

            }).start();
        }

        return Service.START_NOT_STICKY;
    }
}
