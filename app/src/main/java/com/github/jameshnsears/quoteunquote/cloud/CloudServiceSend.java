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

public class CloudServiceSend extends Service {
    public static boolean isRunning = false;
    @NonNull
    public Handler handler = new Handler(Looper.getMainLooper());
    @Nullable
    public CloudFavourites cloudFavourites = getCloudFavourites();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (this) {
            isRunning = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            isRunning = false;
        }
    }

    @Override
    @Nullable
    public IBinder onBind(@NonNull final Intent intent) {
        return null;
    }

    public CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    @Override
    public int onStartCommand(
            @NonNull final Intent intent, final int flags, final int startId) {

        if (!isRunning) {
            synchronized (this) {
                isRunning = true;
            }

            new Thread(() -> {
                final Context context = getServiceContext();

                try {
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
                                    context.getString(R.string.fragment_content_favourites_share_sent, intent.getStringExtra("localCodeValue")),
                                    Toast.LENGTH_LONG));

                            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                            properties.put("code", intent.getStringExtra("localCodeValue"));
                            AuditEventHelper.auditEvent("FAVOURITE_SEND", properties);

                        } else {
                            CloudServiceHelper.showNoNetworkToast(context, handler);
                        }
                    }

                    stopSelf();
                } finally {
                    cloudFavourites.shutdown();
                    synchronized (this) {
                        isRunning = false;
                    }
                }

            }).start();
        }

        return START_NOT_STICKY;
    }

    public Context getServiceContext() {
        return CloudServiceSend.this.getApplicationContext();
    }
}
