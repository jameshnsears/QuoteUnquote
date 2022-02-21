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
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class CloudServiceReceive extends Service {
    private IBinder binder;
    private Handler handler = getHandler();
    private CloudFavourites cloudFavourites = getCloudFavourites();
    public boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new CloudServiceReceiveLocalBinder(this);
    }

    @Override
    public void onDestroy() {
        Timber.d("%b", isRunning);

        isRunning = false;

        handler = null;

        binder = null;

        CloudFavourites.shutdown();
        cloudFavourites = null;

        super.onDestroy();
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
        return binder;
    }

    public void receive(
            @NonNull ContentFragment contentFragment,
            @NonNull String remoteCodeValue) {

        if (!isRunning) {
            isRunning = true;

            new Thread(() -> {
                Timber.d("isRunning=%b", isRunning);

                Context context = getServiceContext();

                if (!cloudFavourites.isInternetAvailable()) {
                    CloudServiceHelper.showNoNetworkToast(context, handler);
                } else {
                    handler.post(() -> ToastHelper.makeToast(
                            context,
                            context.getString(R.string.fragment_content_favourites_share_receiving),
                            Toast.LENGTH_SHORT));

                    List<String> favouritesReceived = cloudFavourites.receive(
                            CloudFavourites.TIMEOUT_SECONDS,
                            CloudFavouritesHelper.jsonReceiveRequest(remoteCodeValue)).digests;

                    if (favouritesReceived == null) {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_missing), Toast.LENGTH_SHORT));
                    } else {
                        DatabaseRepository databaseRepository = getDatabaseRepository(context);
                        for (String digest: favouritesReceived) {
                            if (databaseRepository.getQuotation(digest) != null) {
                                databaseRepository.markAsFavourite(digest);
                            }
                        }

                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_received), Toast.LENGTH_SHORT));

                        if (contentFragment != null) {
                            contentFragment.setFavouriteCount();
                        }

                        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
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

    @Nullable
    public Context getServiceContext() {
        return getApplicationContext();
    }

}
