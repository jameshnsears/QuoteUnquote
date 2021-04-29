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
    public static boolean isRunning;
    @NonNull
    public final Handler handler = new Handler(Looper.getMainLooper());
    @Nullable
    public final CloudFavourites cloudFavourites = this.getCloudFavourites();

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("%b", CloudServiceSend.isRunning);
        CloudServiceSend.isRunning = false;
        CloudFavourites.shutdown();
    }

    @Override
    @Nullable
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(
            @NonNull Intent intent,
            int flags,
            int startId) {

        if (!CloudServiceSend.isRunning) {
            CloudServiceSend.isRunning = true;

            new Thread(() -> {
                Timber.d("isRunning=%b", CloudServiceSend.isRunning);

                Context context = this.getServiceContext();

                if (!this.cloudFavourites.isInternetAvailable()) {
                    CloudServiceHelper.showNoNetworkToast(context, this.handler);
                } else {
                    this.handler.post(() -> ToastHelper.makeToast(
                            context,
                            context.getString(R.string.fragment_content_favourites_share_sending),
                            Toast.LENGTH_SHORT));

                    if (this.cloudFavourites.save(intent.getStringExtra("savePayload"))) {

                        this.handler.post(() -> ToastHelper.makeToast(
                                context,
                                context.getString(R.string.fragment_content_favourites_share_sent),
                                Toast.LENGTH_SHORT));

                        this.auditSend(intent);
                    } else {
                        CloudServiceHelper.showNoNetworkToast(context, this.handler);
                    }
                }

                CloudServiceSend.isRunning = false;
                Timber.d("isRunning=%b", CloudServiceSend.isRunning);

                this.stopSelf();

            }).start();
        }

        return Service.START_NOT_STICKY;
    }

    protected void auditSend(@NonNull final Intent intent) {
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("code", intent.getStringExtra("localCodeValue"));
        AuditEventHelper.auditEvent("FAVOURITE_SEND", properties);
    }

    @NonNull
    public CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    @Nullable
    public Context getServiceContext() {
        return getApplicationContext();
    }
}
