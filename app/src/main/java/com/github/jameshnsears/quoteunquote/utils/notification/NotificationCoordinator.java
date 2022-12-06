package com.github.jameshnsears.quoteunquote.utils.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class NotificationCoordinator {
    @Nullable
    private Context context;
    @NonNull
    private static Map<Integer, String> notificationIdToDigestMap = new HashMap();
    @NonNull
    private static Map<Integer, String> notificationIdToChannelMap = new HashMap();

    public int createNotificationId(@NonNull final String digest) {
        return digest.hashCode();
    }

    public boolean isNotificationShowingQuotation(@NonNull final String digest) {
        return notificationIdToDigestMap.containsKey(createNotificationId(digest));
    }

    public void rememberNotification(
            @NonNull String notificationChannelId,
            int notificationId,
            @NonNull final String digest) {
       notificationIdToDigestMap.put(notificationId, digest);
       notificationIdToChannelMap.put(notificationId, notificationChannelId);

        Timber.d("notificationId=%d; notificationIdToDigestMap.size=%d; notificationIdToChannelMap.size=%d",
                notificationId,notificationIdToDigestMap.size(),notificationIdToChannelMap.size());
    }

    public void dismissNotification(
            @NonNull final Context context,
            @NonNull final NotificationHelper notificationHelper,
            final int notificationId) {
        notificationHelper.dismissNotification(context, notificationId);
        forgetNotification(notificationId);
    }

    public void forgetNotification(final int notificationId) {
       notificationIdToDigestMap.remove(notificationId);
       notificationIdToChannelMap.remove(notificationId);

        Timber.d("notificationId=%d; notificationIdToDigestMap.size=%d; notificationIdToChannelMap.size=%d",
                notificationId,notificationIdToDigestMap.size(),notificationIdToChannelMap.size());
    }

    public String getNotificationChannelId(final int notificationId) {
        return notificationIdToChannelMap.get(notificationId);
    }
}
