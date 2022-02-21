package com.github.jameshnsears.quoteunquote.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

public class NotificationHelper {
    private int notificationId;

    public void displayNotification(@Nullable final Context context, @Nullable final QuotationEntity quotationEntity) {
        if (quotationEntity != null) {
            final CharSequence author = this.restrictAuthorSize(quotationEntity.author);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context, this.createNotificationChannel(context))
                    .setSmallIcon(com.github.jameshnsears.quoteunquote.R.drawable.ic_notification_icon)
                    .setContentTitle(author)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(this.restrictQuotationSize(quotationEntity.quotation)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            this.notificationId += 1;
            notificationManager.notify(this.notificationId, builder.build());
        }
    }

    @NonNull
    public String restrictAuthorSize(@NonNull final String author) {
        final int notificationTitleMaxSize = 20;

        String reducedAuthor = "";
        int cumlativeWordLength = 0;

        for (final String word : author.split(" ")) {
            cumlativeWordLength += word.length();
            if (cumlativeWordLength < notificationTitleMaxSize) {
                reducedAuthor += word + " ";
            } else {
                reducedAuthor += "...";
                break;
            }
        }

        return reducedAuthor;
    }

    @NonNull
    public CharSequence restrictQuotationSize(@NonNull final String quotation) {
        final int notificationBodyMaxSize = 150;

        String reducedQuotation = "";
        int cumlativeWordLength = 0;

        for (final String word : quotation.split(" ")) {
            cumlativeWordLength += word.length();
            if (cumlativeWordLength < notificationBodyMaxSize) {
                reducedQuotation += word + " ";
            } else {
                reducedQuotation += "...";
                break;
            }
        }

        return reducedQuotation;
    }

    @Nullable
    private String createNotificationChannel(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String notificationChannelId = context.getString(com.github.jameshnsears.quoteunquote.R.string.notification_channel_id);

            final NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId,
                    context.getText(com.github.jameshnsears.quoteunquote.R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
            notificationChannel.enableVibration(true);

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return notificationChannelId;
        } else {
            // pre-O (26) devices.
            return null;
        }
    }
}
