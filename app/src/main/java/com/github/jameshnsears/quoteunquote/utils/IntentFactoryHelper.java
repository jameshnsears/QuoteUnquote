package com.github.jameshnsears.quoteunquote.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;

import java.security.SecureRandom;

public class IntentFactoryHelper {
    @NonNull
    public static final String ACTIVITY_FINISHED_CONFIGURATION = "ACTIVITY_FINISHED_CONFIGURATION";
    @NonNull
    public static final String TOOLBAR_PRESSED_FIRST = "TOOLBAR_PRESSED_FIRST";
    @NonNull
    public static final String TOOLBAR_PRESSED_PREVIOUS = "TOOLBAR_PRESSED_PREVIOUS";
    @NonNull
    public static final String TOOLBAR_PRESSED_FAVOURITE = "TOOLBAR_PRESSED_FAVOURITE";
    @NonNull
    public static final String NOTIFICATION_FAVOURITE_PRESSED = "NOTIFICATION_FAVOURITE_PRESSED";
    @NonNull
    public static final String NOTIFICATION_NEXT_PRESSED = "NOTIFICATION_NEXT_PRESSED";
    @NonNull
    public static final String NOTIFICATION_DISMISSED = "NOTIFICATION_DISMISSED";
    @NonNull
    public static final String ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION
            = "ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION";
    @NonNull
    public static final String TOOLBAR_PRESSED_SHARE = "TOOLBAR_PRESSED_SHARE";
    @NonNull
    public static final String TOOLBAR_PRESSED_JUMP = "TOOLBAR_PRESSED_JUMP";
    @NonNull
    public static final String TOOLBAR_PRESSED_NEXT_RANDOM = "TOOLBAR_PRESSED_NEXT_RANDOM";
    @NonNull
    public static final String TOOLBAR_PRESSED_NEXT_SEQUENTIAL = "TOOLBAR_PRESSED_NEXT_SEQUENTIAL";
    @NonNull
    public static final String DAILY_ALARM = "DAILY_ALARM";
    @NonNull
    public static final String BIHOURLY_ALARM = "BIHOURLY_ALARM";
    @NonNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @NonNull
    public static Intent createIntent(final int widgetId) {
        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return resultValue;
    }

    @NonNull
    public static Intent createIntentShare(@NonNull final String subject, @NonNull final String quoteAndAuthor) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, quoteAndAuthor);

        final Intent chooserIntent = Intent.createChooser(shareIntent, null);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooserIntent;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createPendingIntentTemplate(@NonNull final Context context) {
        final Intent pendingIntent = new Intent(context, ConfigureActivity.class);

        PendingIntent clickPendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clickPendingIntent = PendingIntent
                    .getActivity(context, 0,
                            pendingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            clickPendingIntent = PendingIntent
                    .getActivity(context, 0,
                            pendingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

        }

        return clickPendingIntent;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createClickPendingIntent(
            @NonNull final Context context,
            final int uniqueId,
            @NonNull final String action) {
        final Intent intent = createIntent(context, uniqueId);
        intent.setAction(action);

        int pendingIntentFlags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        return PendingIntent.getBroadcast(context, uniqueId, intent, pendingIntentFlags);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createClickPendingIntent(
            @NonNull final Context context,
            final int uniqueId,
            @NonNull final String action,
            @NonNull Bundle bundle) {
        final Intent intent = createIntent(context, uniqueId);
        intent.setAction(action);
        intent.putExtras(bundle);

        int pendingIntentFlags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        return PendingIntent.getBroadcast(context, uniqueId, intent, pendingIntentFlags);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createClickPendingIntent(
            @NonNull final Context context,
            final int widgetId,
            final int uniqueId,
            @NonNull final String action,
            @NonNull Bundle bundle) {
        final Intent intent = createIntent(context, widgetId);
        intent.setAction(action);
        intent.putExtras(bundle);

        int pendingIntentFlags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        return PendingIntent.getBroadcast(context, uniqueId, intent, pendingIntentFlags);
    }

    @NonNull
    public static Intent createClickFillInIntent(@NonNull final String key, @NonNull final String value, final int widgetId) {
        final Intent clickFillInIntent = new Intent();

        final Bundle extras = new Bundle();
        extras.putString(key, value);
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        clickFillInIntent.putExtras(extras);

        return clickFillInIntent;
    }

    @NonNull
    public static Intent createIntent(@NonNull final Context context, final int widgetId) {
        final Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @NonNull
    public static Intent createIntent(
            @NonNull final Context context,
            @NonNull final Class cls,
            final int widgetId) {
        final Intent intent = new Intent(context, cls);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        // support widgetId being sent to correct Widget instance!
        intent.setData(Uri.fromParts("content", String.valueOf(SECURE_RANDOM.nextInt()), null));

        return intent;
    }

    @NonNull
    public static Intent createIntentAction(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final String action) {
        final Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return intent;
    }

    @NonNull
    public static Intent createIntentActionView(@NonNull String url) {
        final Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
