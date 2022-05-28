package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.archive.ArchiveFragment;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import java.util.concurrent.ConcurrentHashMap;

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

                if (!cloudTransfer.isInternetAvailable()) {
                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_archive_internet_missing),
                            Toast.LENGTH_SHORT).show());
                } else {
                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_archive_backup_sending),
                            Toast.LENGTH_SHORT).show());

                    if (cloudTransfer.backup(intent.getStringExtra("asJson"))) {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_backup_success),
                                Toast.LENGTH_SHORT).show());

                        auditBackup(intent);
                    } else {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_archive_internet_missing),
                                Toast.LENGTH_SHORT).show());
                    }
                }

                CloudService.isRunning = false;
                Timber.d("isRunning=%b", CloudService.isRunning);

                this.broadcastEvent(ArchiveFragment.ENABLE_BUTTON_RESTORE);
                this.broadcastEvent(ArchiveFragment.ENABLE_BUTTON_BACKUP);

                stopSelf();

            }).start();
        }

        return Service.START_NOT_STICKY;
    }

    private void auditBackup(@NonNull Intent intent) {
        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("code", intent.getStringExtra("localCodeValue"));
        AuditEventHelper.auditEvent("BACKUP", properties);
    }
}
