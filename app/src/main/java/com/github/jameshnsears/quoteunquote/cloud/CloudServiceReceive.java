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

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class CloudServiceReceive extends Service {
    @NonNull
    private final IBinder binder = new LocalBinder();

    @NonNull
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    @NonNull
    public IBinder onBind(@NonNull final Intent intent) {
        return binder;
    }

    private void showNoNetworkToast(@NonNull final Context context) {
        handler.post(() -> ToastHelper.makeToast(
                context,
                context.getString(R.string.fragment_content_favourites_share_comms),
                Toast.LENGTH_SHORT));
    }

    public void receive(
            @NonNull final ContentFragment contentFragment, @NonNull final String remoteCodeValue) {
        new Thread(() -> {
            final Context context = CloudServiceReceive.this.getApplicationContext();

            final CloudFavourites cloudFavourites = new CloudFavourites();

            try {
                if (!cloudFavourites.isInternetAvailable()) {
                    showNoNetworkToast(context);
                } else {
                    handler.post(() -> ToastHelper.makeToast(
                            context,
                            context.getString(R.string.fragment_content_favourites_share_receiving),
                            Toast.LENGTH_LONG));

                    final List<String> favouritesReceived = cloudFavourites.receive(
                            CloudFavourites.TIMEOUT_SECONDS,
                            CloudFavouritesHelper.receiveRequest(remoteCodeValue));

                    if (favouritesReceived.isEmpty()) {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_missing), Toast.LENGTH_LONG));
                    } else {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_received), Toast.LENGTH_LONG));

                        final DatabaseRepository databaseRepository = DatabaseRepository.getInstance(context);
                        favouritesReceived.forEach(databaseRepository::markAsFavourite);

                        if (contentFragment != null) {
                            contentFragment.setFavouriteCount();
                            contentFragment.enableFavouriteButtonReceive(true);
                        }

                        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                        properties.put("code", remoteCodeValue);
                        AuditEventHelper.auditEvent("FAVOURITE_RECEIVE", properties);
                    }
                }
            } finally {
                cloudFavourites.shutdown();
            }
        }).start();
    }

    public class LocalBinder extends Binder {
        @NonNull
        public CloudServiceReceive getService() {
            return CloudServiceReceive.this;
        }
    }
}
