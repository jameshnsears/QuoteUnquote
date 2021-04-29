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
    @NonNull
    private final IBinder binder = new LocalBinder();
    @NonNull
    private final Handler handler = this.getHandler();
    @NonNull
    private final CloudFavourites cloudFavourites = this.getCloudFavourites();
    public boolean isRunning;

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isRunning = false;
        Timber.d("%b", this.isRunning);
        CloudFavourites.shutdown();
    }

    @NonNull
    protected Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @NonNull
    protected DatabaseRepository getDatabaseRepository(@NonNull final Context context) {
        return DatabaseRepository.getInstance(context);
    }

    @Override
    @NonNull
    public IBinder onBind(@NonNull Intent intent) {
        return this.binder;
    }

    public void receive(
            @NonNull ContentFragment contentFragment,
            @NonNull String remoteCodeValue) {

        if (!this.isRunning) {
            this.isRunning = true;

            new Thread(() -> {
                Timber.d("isRunning=%b", this.isRunning);

                Context context = this.getServiceContext();


                if (!this.cloudFavourites.isInternetAvailable()) {
                    CloudServiceHelper.showNoNetworkToast(context, this.handler);
                } else {
                    this.handler.post(() -> ToastHelper.makeToast(
                            context,
                            context.getString(R.string.fragment_content_favourites_share_receiving),
                            Toast.LENGTH_SHORT));

                    List<String> favouritesReceived = this.cloudFavourites.receive(
                            CloudFavourites.TIMEOUT_SECONDS,
                            CloudFavouritesHelper.jsonReceiveRequest(remoteCodeValue)).digests;

                    if (favouritesReceived == null) {
                        this.handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_missing), Toast.LENGTH_SHORT));
                    } else {
                        this.handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_received), Toast.LENGTH_SHORT));

                        favouritesReceived.forEach(this.getDatabaseRepository(context)::markAsFavourite);

                        if (contentFragment != null) {
                            contentFragment.setFavouriteCount();
                        }

                        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                        properties.put("code", remoteCodeValue);
                        AuditEventHelper.auditEvent("FAVOURITE_RECEIVE", properties);
                    }
                }

                this.isRunning = false;
                Timber.d("isRunning=%b", this.isRunning);

            }).start();
        }
    }

    @NonNull
    protected CloudFavourites getCloudFavourites() {
        return new CloudFavourites();
    }

    @Nullable
    public Context getServiceContext() {
        return getApplicationContext();
    }

    public class LocalBinder extends Binder {
        @NonNull
        public CloudServiceReceive getService() {
            return CloudServiceReceive.this;
        }
    }
}
