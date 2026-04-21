package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CloudService extends Service {
    private static final Object isRunningLock = new Object();
    public static volatile boolean isRunning;
    @Nullable
    public final CloudTransfer cloudTransfer = new CloudTransfer();
    @NonNull
    public Handler handler = new Handler(Looper.getMainLooper());

    public static boolean isRunning() {
        synchronized (isRunningLock) {
            return isRunning;
        }
    }

    public static boolean startRunning() {
        synchronized (isRunningLock) {
            if (!isRunning) {
                isRunning = true;
                return true;
            }
            return false;
        }
    }

    public static void stopRunning() {
        synchronized (isRunningLock) {
            isRunning = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopRunning();
        CloudTransfer.shutdown();
    }

    @Override
    @Nullable
    public IBinder onBind(@NonNull final Intent intent) {
        return null;
    }

    @Nullable
    public Context getServiceContext() {
        return this.getApplicationContext();
    }

    protected void broadcastEvent(@NonNull String event) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        broadcaster.sendBroadcast(new Intent(event));
    }
}
