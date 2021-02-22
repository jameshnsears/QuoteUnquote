package com.github.jameshnsears.quoteunquote.cloud;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
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
    @NonNull
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    public CloudFavourites cloudFavourites = getCloudFavourites();

    public static boolean isRunning(@NonNull final Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (final ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.getClassName().equals(CloudServiceSend.class.getName())) {
                return true;
            }
        }
        return false;
    }

    public class LocalBinder extends Binder {
        CloudServiceSend getService() {
            return CloudServiceSend.this;
        }
    }

    @Override
    @Nullable
    public IBinder onBind(@NonNull final Intent intent) {
        return new LocalBinder();
    }

    private void showNoNetworkToast(@NonNull final Context context) {
        handler.post(() -> ToastHelper.makeToast(
                context,
                context.getString(R.string.fragment_content_favourites_share_comms),
                Toast.LENGTH_SHORT));
    }

    public CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    @Override
    public int onStartCommand(
            @NonNull final Intent intent, final int flags, final int startId) {
        new Thread(() -> {
            final Context context = CloudServiceSend.this.getApplicationContext();

            try {
                if (!cloudFavourites.isInternetAvailable()) {
                    showNoNetworkToast(context);
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
                        showNoNetworkToast(context);
                    }
                }

                stopSelf();
            } finally {
                cloudFavourites.shutdown();
            }

        }).start();

        return START_NOT_STICKY;
    }
}
