package com.github.jameshnsears.quoteunquote.cloud;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.FragmentContent;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;

import java.util.List;

public class CloudServiceReceive extends Service {
    private static final String LOG_TAG = CloudServiceReceive.class.getSimpleName();

    private final IBinder binder = new LocalBinder();

    protected Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void showNoNetworkToast(Context context) {
        handler.post(() -> ToastHelper.makeToast(
                context,
                context.getString(R.string.fragment_content_favourites_share_comms),
                Toast.LENGTH_SHORT));
    }

    public void receive(FragmentContent fragmentContent, String remoteCodeValue) {
        Log.d(LOG_TAG, String.format("%s", new Object() {
        }.getClass().getEnclosingMethod().getName()));

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
                            CloudFavourites.TIMEOUT,
                            CloudFavouritesHelper.receiveRequest(remoteCodeValue));

                    if (favouritesReceived.isEmpty()) {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_missing), Toast.LENGTH_LONG));
                    } else {
                        handler.post(() -> ToastHelper.makeToast(
                                context, context.getString(R.string.fragment_content_favourites_share_received), Toast.LENGTH_LONG));

                        final DatabaseRepository databaseRepository = new DatabaseRepository(context);
                        favouritesReceived.forEach(databaseRepository::markAsFavourite);

                        if (fragmentContent != null) {
                            fragmentContent.setCountFavourites();
                            fragmentContent.enableFavouriteReceiveButton(true);
                        }

                        CloudFavouritesHelper.auditFavourites(
                                AuditEventHelper.FAVOURITE_RECEIVE,
                                remoteCodeValue);
                    }
                }
            } finally {
                cloudFavourites.shutdown();
            }
        }).start();
    }

    public class LocalBinder extends Binder {
        public CloudServiceReceive getService() {
            return CloudServiceReceive.this;
        }
    }
}
