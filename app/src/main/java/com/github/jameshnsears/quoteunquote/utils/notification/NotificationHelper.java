package com.github.jameshnsears.quoteunquote.utils.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import timber.log.Timber;


public class NotificationHelper {
    public void displayNotification(
            @NonNull Context context,
            int widgetId,
            @NonNull String author,
            @NonNull String quotation,
            @NonNull String digest,
            boolean isFavourite,
            boolean sequential,
            int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, createNotificationChannel(context))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(author)
                .setStyle(getBigTextStyle(quotation))
                .setDeleteIntent(createNotificationDeleteIntent(context, widgetId, digest))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        builder.addAction(getActionFavourite(context, widgetId, digest, notificationId, isFavourite));
        builder.addAction(getActionNext(context, widgetId, digest, notificationId, sequential));

        NotificationManagerCompat.from(context)
                .notify(notificationId, builder.build());
    }

    private PendingIntent createNotificationDeleteIntent(
            @Nullable Context context,
            final int widgetId,
            @NonNull String digest) {
        Bundle bundle = new Bundle();
        bundle.putString("widgetId", digest);
        bundle.putString("digest", digest);

        return IntentFactoryHelper.createClickPendingIntent(
                context,
                widgetId,
                IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_DELETED,
                bundle
        );
    }

    private NotificationCompat.Action getActionFavourite(
            @NonNull Context context,
            final int widgetId,
            @NonNull String digest,
            final int notificationId,
            boolean isFavourite) {
        Bundle bundle = new Bundle();
        bundle.putString("digest", digest);
        bundle.putInt("notificationId", notificationId);

        Timber.d("digest=%s; notificationId=%d", digest, notificationId);

        int icon = R.drawable.ic_toolbar_favorite_ff000000_24;
        if (isFavourite) {
            icon = R.drawable.ic_toolbar_favorite_red_24;
        }

        return new NotificationCompat.Action(icon,
                context.getString(R.string.notification_action_favourite),
                IntentFactoryHelper.createClickPendingIntent(
                        context,
                        widgetId,
                        IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_FAVOURITE,
                        bundle
                )
        );
    }

    private NotificationCompat.Action getActionNext(
            @NonNull Context context,
            final int widgetId,
            @NonNull String digest,
            final int notificationId,
            boolean sequential) {
        Bundle bundle = new Bundle();
        bundle.putString("digest", digest);
        bundle.putInt("notificationId", notificationId);

        Timber.d("digest=%s; notificationId=%d", digest, notificationId);

        int icon = R.drawable.ic_toolbar_next_sequential_ff000000_24;
        String nextString = context.getString(R.string.fragment_appearance_toolbar_next_sequential);
        if (!sequential) {
            icon = R.drawable.ic_toolbar_next_random_ff000000_24;
            nextString = context.getString(R.string.fragment_appearance_toolbar_next_random);
        }

        return new NotificationCompat.Action(
                icon,
                nextString,
                IntentFactoryHelper.createClickPendingIntent(
                        context,
                        widgetId,
                        IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_NEXT,
                        bundle
                )
        );
    }

    @NonNull
    private NotificationCompat.BigTextStyle getBigTextStyle(@Nullable String quotation) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(quotation);
        return bigTextStyle;
    }

    @Nullable
    private String createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String notificationChannelId
                    = context.getString(com.github.jameshnsears.quoteunquote.R.string.notification_channel_id);

            NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId,
                    context.getText(com.github.jameshnsears.quoteunquote.R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return notificationChannelId;
        } else {
            // pre-O (26) devices.
            return null;
        }
    }
}
