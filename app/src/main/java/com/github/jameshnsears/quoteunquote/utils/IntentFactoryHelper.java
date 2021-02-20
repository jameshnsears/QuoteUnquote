package com.github.jameshnsears.quoteunquote.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;

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
    public static Intent createIntent(final int widgetId) {
        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return resultValue;
    }

    @NonNull
    public static Intent createIntentShare(@NonNull final String subject, @NonNull final String quote) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, quote);

        final Intent chooserIntent = Intent.createChooser(shareIntent, null);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooserIntent;
    }

    @NonNull
    public static PendingIntent createIntentPending(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final String action) {
        final Intent intent = createIntent(context, widgetId);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    public static Intent createIntentActionView() {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://github.com/jameshnsears/quoteunquote"));
        return intent;
    }
}
