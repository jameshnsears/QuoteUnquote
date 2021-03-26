package com.github.jameshnsears.quoteunquote.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;

import java.security.SecureRandom;

public class IntentFactoryHelper {
    public static final String ACTIVITY_FINISHED_CONFIGURATION = "ACTIVITY_FINISHED_CONFIGURATION";
    public static final String ACTIVITY_FINISHED_REPORT = "ACTIVITY_FINISHED_REPORT";

    public static final String TOOLBAR_PRESSED_FIRST = "TOOLBAR_PRESSED_FIRST";
    public static final String TOOLBAR_PRESSED_FAVOURITE = "TOOLBAR_PRESSED_FAVOURITE";
    public static final String TOOLBAR_PRESSED_SHARE = "TOOLBAR_PRESSED_SHARE";
    public static final String TOOLBAR_PRESSED_NEXT = "TOOLBAR_PRESSED_NEXT";

    public static final String DAILY_ALARM = "DAILY_ALARM";

    private static SecureRandom secureRandom = new SecureRandom();

    public static Intent createIntent(final int widgetId) {
        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return resultValue;
    }

    public static Intent createIntentShare(final String subject, final String quote) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, quote);

        final Intent chooserIntent = Intent.createChooser(shareIntent, null);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooserIntent;
    }

    public static PendingIntent createIntentPending(final Context context, final int widgetId, final String action) {
        final Intent intent = createIntent(context, widgetId);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createIntent(final Context context, final int widgetId) {
        final Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    public static Intent createIntent(final Context context, final Class cls, final int widgetId) {
        final Intent intent = new Intent(context, cls);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        // support widgetId being sent to correct Widget instance!
        intent.setData(Uri.fromParts("content", String.valueOf(secureRandom.nextInt()), null));

        return intent;
    }

    public static Intent createIntentAction(final Context context, final int widgetId, final String action) {
        final Intent intent = new Intent(context, QuoteUnquoteWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setAction(action);
        return intent;
    }

    public static Intent createIntentActionView() {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://github.com/jameshnsears/quoteunquote"));
        return intent;
    }
}
