package com.github.jameshnsears.quoteunquote.cloud;

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
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class CloudServiceReceive extends Service {
    public boolean isRunning = false;
    @NonNull
    private final IBinder binder = new LocalBinder();
    @NonNull
    private final Handler handler = getHandler();
    @NonNull
    private final CloudFavourites cloudFavourites = getCloudFavourites();

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Timber.d("%b", isRunning);
        CloudFavourites.shutdown();
    }

    @NonNull
    protected Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @NonNull
    protected DatabaseRepository getDatabaseRepository(@NonNull Context context) {
        return DatabaseRepository.getInstance(context);
    }

    @Override
    @NonNull
    public IBinder onBind(@NonNull final Intent intent) {
        return binder;
    }

    public void receive(
            @NonNull final ContentFragment contentFragment,
            @NonNull final String remoteCodeValue) {

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
                            context.getString(R.string.fragment_content_favourites_share_receiving),
                            Toast.LENGTH_LONG));

                    final List<String> favouritesReceived = cloudFavourites.receive(
                            CloudFavourites.TIMEOUT_SECONDS,
                            CloudFavouritesHelper.jsonReceiveRequest(remoteCodeValue)).digests;

                    if (favouritesReceived == null) {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_missing), Toast.LENGTH_LONG));
                    } else {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_received), Toast.LENGTH_LONG));

                        favouritesReceived.forEach(getDatabaseRepository(context)::markAsFavourite);

                        if (contentFragment != null) {
                            contentFragment.setFavouriteCount();
                        }

                        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                        properties.put("code", remoteCodeValue);
                        AuditEventHelper.auditEvent("FAVOURITE_RECEIVE", properties);
                    }
                }

                isRunning = false;
                Timber.d("isRunning=%b", isRunning);

            }).start();
        }
    }

    @NonNull
    protected CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    public class LocalBinder extends Binder {
        @NonNull
        public CloudServiceReceive getService() {
            return CloudServiceReceive.this;
        }
    }

    @Nullable
    public Context getServiceContext() {
        return CloudServiceReceive.this.getApplicationContext();
    }
}
