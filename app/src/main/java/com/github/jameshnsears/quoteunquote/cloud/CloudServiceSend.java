package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class CloudServiceSend extends Service {
    public static boolean isRunning = false;
    @NonNull
    public final Handler handler = new Handler(Looper.getMainLooper());
    @Nullable
    public final CloudFavourites cloudFavourites = getCloudFavourites();

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("%b", isRunning);
        isRunning = false;
        CloudFavourites.shutdown();
    }

    @Override
    @Nullable
    public IBinder onBind(@NonNull final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(
            @NonNull final Intent intent,
            final int flags,
            final int startId) {

        if (!isRunning) {
            isRunning = true;

            new Thread(() -> {
                Timber.d("isRunning=%b", isRunning);

                final Context context = getServiceContext();

                if (!cloudFavourites.isInternetAvailable()) {
                    CloudServiceHelper.showNoNetworkToast(context, handler);
                } else {
                    handler.post(() -> ToastHelper.makeToast(
                            context,
                            context.getString(R.string.fragment_content_favourites_share_sending),
                            Toast.LENGTH_LONG));

                    if (cloudFavourites.save(intent.getStringExtra("savePayload"))) {

                        handler.post(() -> ToastHelper.makeToast(
                                context,
                                context.getString(R.string.fragment_content_favourites_share_sent),
                                Toast.LENGTH_LONG));

                        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                        properties.put("code", intent.getStringExtra("localCodeValue"));
                        AuditEventHelper.auditEvent("FAVOURITE_SEND", properties);

                    } else {
                        CloudServiceHelper.showNoNetworkToast(context, handler);
                    }
                }

                isRunning = false;
                Timber.d("isRunning=%b", isRunning);

                stopSelf();

            }).start();
        }

        return START_NOT_STICKY;
    }

    @NonNull
    public CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    @Nullable
    public Context getServiceContext() {
        return CloudServiceSend.this.getApplicationContext();
    }
}
