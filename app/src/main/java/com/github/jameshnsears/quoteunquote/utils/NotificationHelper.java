package com.github.jameshnsears.quoteunquote.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

public class NotificationHelper {
    private int notificationId = 0;

    public void displayNotification(int widgetId, @NonNull Context context, @NonNull QuotationEntity quotationEntity) {
        ContentSelection contentSelection = new ContentPreferences(widgetId, context).getContentSelection();

        CharSequence author = quotationEntity.author;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, createNotificationChannel(context))
                .setSmallIcon(com.github.jameshnsears.quoteunquote.R.drawable.ic_notification_icon)
                .setContentTitle(author)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(restrictQuotationSize(quotationEntity.quotation)))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationId += 1;
        notificationManager.notify(notificationId, builder.build());
    }

    public CharSequence restrictQuotationSize(String quotation) {

        String reducedQuotation = quotation;

        if (quotation.length() > 180) {
            reducedQuotation = quotation.substring(0, 180) + "...";
        }
        return (CharSequence) reducedQuotation;
    }

    private String createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String notificationChannelId = context.getString(com.github.jameshnsears.quoteunquote.R.string.notification_channel_id);

            NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId,
                    context.getText(com.github.jameshnsears.quoteunquote.R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
            notificationChannel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return notificationChannelId;
        } else {
            // pre-O (26) devices.
            return null;
        }
    }
}
