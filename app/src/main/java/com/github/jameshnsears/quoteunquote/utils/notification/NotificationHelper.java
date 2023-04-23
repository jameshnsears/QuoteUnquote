package com.github.jameshnsears.quoteunquote.utils.notification;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
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
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import timber.log.Timber;

public class NotificationHelper {
    /*
    API 30:
    Notification 1 + N
    ...
    Notification 1
     */
    @Nullable
    String notificationChannelDeviceUnlock;
    @Nullable
    String notificationChannelEventDaily;
    @Nullable
    String notificationChannelBihourly;

    public NotificationHelper(@NonNull Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        final String channelGroupId = "group_id_01";

        notificationChannelDeviceUnlock = createNotificationChannel(
                context,
                context.getString(R.string.notification_channel_screen_unlock),
                channelGroupId);

        notificationChannelEventDaily = createNotificationChannel(
                context,
                context.getString(R.string.notification_channel_specific_time),
                channelGroupId);

        notificationChannelBihourly = createNotificationChannel(
                context,
                context.getString(R.string.notification_channel_every_two_hours),
                channelGroupId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel("Quotations");
        }
    }

    public void displayNotificationDeviceUnlock(
            @NonNull NotificationContent notificationContent) {
        displayNotification(
                notificationContent,
                notificationChannelDeviceUnlock,
                R.drawable.ic_notification_icon_screen_unlock
        );
    }

    public void displayNotificationEventDaily(
            @NonNull NotificationContent notificationContent) {
        displayNotification(
                notificationContent,
                notificationChannelEventDaily,
                R.drawable.ic_notification_icon_specific_time
        );
    }

    public void displayNotificationBihourly(
            @NonNull NotificationContent notificationContent) {
        displayNotification(
                notificationContent,
                notificationChannelBihourly,
                R.drawable.ic_notification_icon_every_two_hours
        );
    }

    @SuppressLint("MissingPermission")
    private void displayNotification(
            @NonNull NotificationContent notificationContent,
            @NonNull String notificationChannelId,
            final int notificationIcon) {

        Timber.d("notificationChannelId=%s", notificationChannelId);
        Timber.d("widgetId=%d; digest=%s; notificationId=%d; notificationEvent=%s",
                notificationContent.getWidgetId(),
                notificationContent.getDigest(),
                notificationContent.getNotificationId(),
                notificationContent.getNotificationEvent()
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                notificationContent.getContext(), notificationChannelId)
                .setSmallIcon(notificationIcon)

                .setDeleteIntent(createNotificationDeleteIntent(
                        notificationContent.getContext(),
                        notificationContent.getWidgetId(),
                        notificationContent.getDigest(),
                        notificationContent.getNotificationId(),
                        notificationContent.getNotificationEvent()))

                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(notificationChannelId);

        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(
                notificationContent.getWidgetId(),
                notificationContent.getContext()
        );
        if (notificationsPreferences.getExcludeSourceFromNotification() == false) {
            builder.setContentTitle(notificationContent.getAuthor());
        }
        builder.setStyle(getBigTextStyle(notificationContent.getQuotation()));

        builder.addAction(getActionFavourite(
                notificationContent.getContext(),
                notificationContent.getWidgetId(),
                notificationContent.getDigest(),
                notificationContent.getNotificationId(),
                notificationContent.getNotificationEvent(),
                notificationContent.isFavourite()));

        builder.addAction(getActionNext(
                notificationContent.getContext(),
                notificationContent.getWidgetId(),
                notificationContent.getDigest(),
                notificationContent.getNotificationId(),
                notificationContent.getNotificationEvent(),
                notificationContent.getSequential()));

        NotificationManagerCompat.from(notificationContent.getContext())
                .notify(notificationContent.getNotificationId(), builder.build());
    }

    public void dismissNotification(@NonNull Context context, final int notificationId) {
        Timber.d("notificationId=%d", notificationId);

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @NonNull
    private PendingIntent createNotificationDeleteIntent(
            @Nullable Context context,
            final int widgetId,
            @NonNull String digest,
            final int notificationId,
            @NonNull final String notificationEvent) {
        return IntentFactoryHelper.createClickPendingIntent(
                context,
                notificationId,
                IntentFactoryHelper.NOTIFICATION_DISMISSED,
                getActionBundle(widgetId, digest, notificationId, notificationEvent)
        );
    }

    @NonNull
    private NotificationCompat.Action getActionFavourite(
            @NonNull Context context,
            final int widgetId,
            @NonNull String digest,
            final int notificationId,
            @NonNull final String notificationEvent,
            boolean isFavourite) {
        int icon = R.drawable.ic_toolbar_favorite_ff000000_24;
        if (isFavourite) {
            icon = R.drawable.ic_toolbar_favorite_red_24;
        }

        PendingIntent pendingIntent = IntentFactoryHelper.createClickPendingIntent(
                context,
                widgetId,
                notificationId,
                IntentFactoryHelper.NOTIFICATION_FAVOURITE_PRESSED,
                getActionBundle(widgetId, digest, notificationId, notificationEvent)
        );

        String actionString = context.getString(R.string.notification_action_unfavourite);
        if (!isFavourite) {
            actionString = context.getString(R.string.notification_action_favourite);
        }

        return new NotificationCompat.Action(icon,
                actionString,
                pendingIntent
        );
    }

    @NonNull
    private Bundle getActionBundle(
            int widgetId,
            @NonNull String digest,
            int notificationId,
            @NonNull final String notificationEvent) {
        Bundle bundle = new Bundle();
        bundle.putInt("widgetId", widgetId);
        bundle.putString("digest", digest);
        bundle.putInt("notificationId", notificationId);
        bundle.putString("notificationEvent", notificationEvent);
        return bundle;
    }

    @NonNull
    private NotificationCompat.Action getActionNext(
            @NonNull Context context,
            final int widgetId,
            @NonNull String digest,
            final int notificationId,
            @NonNull final String notificationEvent,
            boolean sequential) {
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
                        notificationId,
                        IntentFactoryHelper.NOTIFICATION_NEXT_PRESSED,
                        getActionBundle(widgetId, digest, notificationId, notificationEvent)
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
    private String createNotificationChannel(
            @NonNull Context context,
            @NonNull String notificationChannelId,
            @NonNull String notificationChannelGroupId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            NotificationChannelGroup notificationChannelGroup = new NotificationChannelGroup(
                    notificationChannelGroupId,
                    context.getString(R.string.fragment_notifications_recurring_event));
            notificationManager.createNotificationChannelGroup(notificationChannelGroup);

            NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId,
                    notificationChannelId,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(notificationChannelId);
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(false);
            notificationChannel.setGroup(notificationChannelGroupId);

            notificationManager.createNotificationChannel(notificationChannel);

            return notificationChannelId;
        } else {
            // pre-O (26) devices.
            return null;
        }
    }
}
