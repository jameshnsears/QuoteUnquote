package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferRestoreResponse;
import com.github.jameshnsears.quoteunquote.cloud.transfer.restore.TransferRestore;
import com.github.jameshnsears.quoteunquote.configure.fragment.transfer.TransferFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import java.util.concurrent.ConcurrentHashMap;

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

                if (!cloudTransfer.isInternetAvailable()) {
                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_transfer_internet_missing),
                            Toast.LENGTH_SHORT).show());
                } else {
                    auditRestore(intent);

                    handler.post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.fragment_transfer_restore_receiving),
                            Toast.LENGTH_SHORT).show());

                    TransferRestoreResponse transferRestoreResponse
                            = cloudTransfer.restore(
                                    CloudTransfer.TIMEOUT_SECONDS,
                                    new TransferRestore().requestJson(
                                            intent.getStringExtra("remoteCodeValue")));

                    if (transferRestoreResponse.getReason().equals("no JSON for code")) {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_transfer_restore_missing_code),
                                Toast.LENGTH_SHORT).show());
                    }
                    else if (transferRestoreResponse == null) {
                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_transfer_internet_missing),
                                Toast.LENGTH_SHORT).show());
                    }
                    else {
                        TransferRestore transferRestore = new TransferRestore();
                        transferRestore.restore(
                                context,
                                DatabaseRepository.getInstance(context),
                                transferRestoreResponse.getTransfer());

                        handler.post(() -> Toast.makeText(
                                context,
                                context.getString(R.string.fragment_transfer_restore_success),
                                Toast.LENGTH_SHORT).show());
                    }

                    broadcastEvent(TransferFragment.ENABLE_BUTTON_RESTORE);
                    broadcastEvent(TransferFragment.ENABLE_BUTTON_BACKUP);
                }

                CloudService.isRunning = false;
                Timber.d("isRunning=%b", CloudService.isRunning);

                stopSelf();

            }).start();
        }

        return Service.START_NOT_STICKY;
    }

    protected void auditRestore(@NonNull Intent intent) {
        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("code", intent.getStringExtra("remoteCodeValue"));
        AuditEventHelper.auditEvent("RESTORE", properties);
    }
}