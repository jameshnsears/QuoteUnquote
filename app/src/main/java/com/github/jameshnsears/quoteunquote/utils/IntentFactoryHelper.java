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
    public static final String ACTIVITY_FINISHED_REPORT = "ACTIVITY_FINISHED_REPORT";
    @NonNull
    public static final String TOOLBAR_PRESSED_FIRST = "TOOLBAR_PRESSED_FIRST";
    @NonNull
    public static final String TOOLBAR_PRESSED_PREVIOUS = "TOOLBAR_PRESSED_PREVIOUS";
    @NonNull
    public static final String TOOLBAR_PRESSED_FAVOURITE = "TOOLBAR_PRESSED_FAVOURITE";
    @NonNull
    public static final String ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION
            = "ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION";
    @NonNull
    public static final String TOOLBAR_PRESSED_SHARE = "TOOLBAR_PRESSED_SHARE";
    @NonNull
    public static final String TOOLBAR_PRESSED_NEXT_RANDOM = "TOOLBAR_PRESSED_NEXT_RANDOM";
    @NonNull
    public static final String TOOLBAR_PRESSED_NEXT_SEQUENTIAL = "TOOLBAR_PRESSED_NEXT_SEQUENTIAL";
    @NonNull
    public static final String DAILY_ALARM = "DAILY_ALARM";
    @NonNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @NonNull
    public static Intent createIntent(int widgetId) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return resultValue;
    }

    @NonNull
    public static Intent createIntentShare(@NonNull String subject, @NonNull String quoteAndAuthor) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, quoteAndAuthor);

        Intent chooserIntent = Intent.createChooser(shareIntent, null);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooserIntent;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createPendingIntentTemplate(@NonNull Context context) {
        Intent pendingIntent = new Intent(context, ConfigureActivity.class);

        PendingIntent clickPendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clickPendingIntent = PendingIntent
                    .getActivity(context, 0,
                            pendingIntent ,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            clickPendingIntent = PendingIntent
                    .getActivity(context, 0,
                            pendingIntent ,
                            PendingIntent.FLAG_UPDATE_CURRENT);

        }

        return clickPendingIntent;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @NonNull
    public static PendingIntent createClickPendingIntent(
            @NonNull Context context,
            int widgetId,
            @NonNull String action) {
        Intent intent = IntentFactoryHelper.createIntent(context, widgetId);
        intent.setAction(action);

        int pendingIntentFlags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        return PendingIntent.getBroadcast(context, widgetId, intent, pendingIntentFlags);
    }

    @NonNull
    public static Intent createClickFillInIntent(@NonNull String key, @NonNull String value, int widgetId) {
        Intent clickFillInIntent = new Intent();

        Bundle extras = new Bundle();
        extras.putString(key, value);
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        clickFillInIntent.putExtras(extras);

        return clickFillInIntent;
    }

    @NonNull
    public static Intent createIntent(@NonNull Context context, int widgetId) {
        Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull Class cls,
            int widgetId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        // support widgetId being sent to correct Widget instance!
        intent.setData(Uri.fromParts("content", String.valueOf(IntentFactoryHelper.SECURE_RANDOM.nextInt()), null));

        return intent;
    }

    @NonNull
    public static Intent createIntentAction(
            @NonNull Context context,
            int widgetId,
            @NonNull String action) {
        Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return intent;
    }

    @NonNull
    public static Intent createIntentActionView(@NonNull final String url) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
