package com.github.jameshnsears.quoteunquote.cloud;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;

import androidx.annotation.Nullable;

public class CloudServiceSend extends Service {
    private static final String LOG_TAG = CloudServiceSend.class.getSimpleName();

    protected Handler handler = new Handler(Looper.getMainLooper());

    public static boolean isRunning(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (final ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.getClassName().equals(CloudServiceSend.class.getName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, String.format("%s", new Object() {
        }.getClass().getEnclosingMethod().getName()));

        super.onDestroy();
    }

    private void showNoNetworkToast(Context context) {
        handler.post(() -> ToastHelper.makeToast(
                context,
                context.getString(R.string.fragment_content_favourites_share_comms),
                Toast.LENGTH_SHORT));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, String.format("%s", new Object() {
        }.getClass().getEnclosingMethod().getName()));

        new Thread(() -> {
            final Context context = CloudServiceSend.this.getApplicationContext();

            final CloudFavourites cloudFavourites = new CloudFavourites();

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

                        CloudFavouritesHelper.auditFavourites(
                                AuditEventHelper.FAVOURITE_SEND,
                                intent.getStringExtra("localCodeValue"));
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
