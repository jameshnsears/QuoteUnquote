package com.github.jameshnsears.quoteunquote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceSend;
import com.github.jameshnsears.quoteunquote.configure.ActivityConfigure;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
import com.github.jameshnsears.quoteunquote.report.ActivityReport;
import com.github.jameshnsears.quoteunquote.utils.DailyAlarm;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DISABLED;

public class QuoteUnquoteWidget extends AppWidgetProvider {
    private static final String LOG_TAG = QuoteUnquoteWidget.class.getSimpleName();

    private QuoteUnquoteModel quoteUnquoteModel;

    public synchronized QuoteUnquoteModel getQuoteUnquoteModelInstance(final Context context) {
        if (quoteUnquoteModel == null) {
            quoteUnquoteModel = new QuoteUnquoteModel(context);
        }

        return quoteUnquoteModel;
    }

    public void shutdownQuoteUnquoteModel(final Context context) {
        if (quoteUnquoteModel != null) {
            getQuoteUnquoteModelInstance(context).shutdown();
            quoteUnquoteModel = null;
        }
    }

    @Override
    public void onEnabled(final Context context) {
        Log.d(LOG_TAG, String.format("%s", new Object() {
        }.getClass().getEnclosingMethod().getName()));

        CloudFavouritesHelper.setSharedPreferenceLocalCode(context);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] widgetIds) {
        for (final int widgetId : widgetIds) {

            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

            remoteViews.setRemoteAdapter(
                    R.id.listViewQuotation,
                    IntentFactoryHelper.createIntent(context, ListViewService.class, widgetId));

            remoteViews.setPendingIntentTemplate(
                    R.id.listViewQuotation,
                    PendingIntent.getActivity(
                            context,
                            widgetId,
                            IntentFactoryHelper.createIntent(context, ActivityConfigure.class, widgetId),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFirst,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FIRST));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonReport,
                    PendingIntent.getActivity(
                            context,
                            widgetId,
                            IntentFactoryHelper.createIntent(context, ActivityReport.class, widgetId),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFavourite,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonShare,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_SHARE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNew,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT));

            setTransparency(context, widgetId, remoteViews);

            setToolbarVisibility(context, widgetId, remoteViews);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        final int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d(LOG_TAG, String.format("%d: %s: action=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                intent.getAction()));

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        final DailyAlarm dailyAlarm = new DailyAlarm(context, widgetId);

        try {
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    onReceiveDeviceUnlock(context, appWidgetManager);
                    break;

                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_REBOOT:
                /*
                adb shell
                am broadcast -a android.intent.action.BOOT_COMPLETED
                 */
                case IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION:
                    onReceiveActivityFinishedConfiguration(widgetId, appWidgetManager, dailyAlarm);
                    break;

                case IntentFactoryHelper.ACTIVITY_FINISHED_REPORT:
                    onReceiveActivityFinishedReport(widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    onReceiveDailyAlarm(context, widgetId, appWidgetManager, dailyAlarm);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FIRST:
                    onReceiveToolbarPressedFirst(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE:
                    onReceiveToolbarPressedFavourite(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_SHARE:
                    onReceiveToolbarPressedShare(context, widgetId);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT:
                    onReceiveToolbarPressedNext(context, widgetId, appWidgetManager);
                    break;

                default:
                    break;
            }
        } finally {
            // mainly for screen rotation!
            if (!intent.getAction().equals(ACTION_APPWIDGET_DISABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveActivityFinishedReport(final int widgetId, final AppWidgetManager appWidgetManager) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDeviceUnlock(final Context context, final AppWidgetManager appWidgetManager) {
        unlockDevice(context, appWidgetManager);
    }

    private void onReceiveToolbarPressedShare(final Context context, final int widgetId) {
        final Preferences preferences = new Preferences(widgetId, context);
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                getQuoteUnquoteModelInstance(context).getNext(widgetId, preferences.getSelectedContentType()).theQuotation()));
    }

    private void onReceiveToolbarPressedFavourite(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager) {
        final Preferences preferences = new Preferences(widgetId, context);

        getQuoteUnquoteModelInstance(context).toggleFavourite(widgetId, getQuoteUnquoteModelInstance(context).getNext(
                widgetId, preferences.getSelectedContentType()).digest);

        toggleFavouriteColour(widgetId, context, appWidgetManager);

        if (preferences.getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES)
                || preferences.getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL)) {
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void onReceiveToolbarPressedFirst(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager) {
        ToastHelper.makeToast(context, context.getString(R.string.widget_button_first_toast), Toast.LENGTH_SHORT);

        final Preferences preferences = new Preferences(widgetId, context);
        getQuoteUnquoteModelInstance(context).deletePrevious(widgetId, preferences.getSelectedContentType());

        toggleFavouriteColour(widgetId, context, appWidgetManager);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedNext(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager) {
        final Preferences preferences = new Preferences(widgetId, context);

        try {
            getQuoteUnquoteModelInstance(context).setNext(widgetId, preferences.getSelectedContentType());
            toggleFavouriteColour(widgetId, context, appWidgetManager);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        } catch (NoNextQuotationAvailableException e) {
            Log.w(LOG_TAG, e.getMessage());
            ToastHelper.makeToast(context, context.getString(R.string.widget_button_next_toast), Toast.LENGTH_SHORT);
        }
    }

    private void onReceiveDailyAlarm(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager, final DailyAlarm dailyAlarm) {
        dailyAlarm.setDailyAlarm();
        try {
            final Preferences preferences = new Preferences(widgetId, context);
            getQuoteUnquoteModelInstance(context).setNext(widgetId, preferences.getSelectedContentType());
            toggleFavouriteColour(widgetId, context, appWidgetManager);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        } catch (NoNextQuotationAvailableException e) {
            Log.w(LOG_TAG, e.getMessage());
        }
    }

    private void onReceiveActivityFinishedConfiguration(final int widgetId, final AppWidgetManager appWidgetManager, final DailyAlarm dailyAlarm) {
        dailyAlarm.setDailyAlarm();
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void setTransparency(final Context context, final int widgetId, final RemoteViews remoteViews) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final Preferences preferences = new Preferences(widgetId, context);

        final int seekBarValue = preferences.getSharedPreferenceInt(
                Preferences.FRAGMENT_APPEARANCE,
                Preferences.SEEK_BAR);
        Log.d(LOG_TAG, "seekBarProgress=" + seekBarValue);

        float transparency = 1;
        if (seekBarValue != -1) {
            transparency -= seekBarValue * .1f;
        }

        String setBackgroundColor = "setBackgroundColor";
        remoteViews.setInt(R.id.listViewQuotation, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
        remoteViews.setInt(R.id.imageButtonFirst, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
        remoteViews.setInt(R.id.imageButtonReport, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
        remoteViews.setInt(R.id.imageButtonFavourite, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
        remoteViews.setInt(R.id.imageButtonShare, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
        remoteViews.setInt(R.id.imageButtonNew, setBackgroundColor, (int) (transparency * 0xFF) << 24 | 0xFFFFFF);
    }

    private void setToolbarVisibility(final Context context, final int widgetId, final RemoteViews remoteViews) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final Preferences preferences = new Preferences(widgetId, context);
        if (preferences.getSharedPreferenceBoolean(
                "FragmentAppearance", "checkBoxDisplayToolbar", true)) {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.GONE);
        }

        setHeartColour(context, widgetId, remoteViews, preferences);
    }

    private void toggleFavouriteColour(
            final int widgetId, final Context context, final AppWidgetManager appWidgetManager) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

        final Preferences preferences = new Preferences(widgetId, context);

        setHeartColour(context, widgetId, remoteViews, preferences);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    private void setHeartColour(
            final Context context, final int widgetId, final RemoteViews remoteViews, final Preferences preferences) {

        final QuotationEntity quotationEntity = getQuoteUnquoteModelInstance(context).getNext(widgetId, preferences.getSelectedContentType());

        if (quotationEntity != null) {
            if (getQuoteUnquoteModelInstance(context).isFavourite(widgetId, quotationEntity.digest)) {
                remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_red_24dp);
            } else {
                remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    private void unlockDevice(final Context context, final AppWidgetManager appWidgetManager) {
        for (final int widgetId : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {

            final Preferences preferences = new Preferences(widgetId, context);

            if (preferences.getSharedPreferenceBoolean(
                    Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DEVICE_UNLOCK)) {
                try {
                    getQuoteUnquoteModelInstance(context).setNext(widgetId, preferences.getSelectedContentType());

                    toggleFavouriteColour(widgetId, context, appWidgetManager);
                    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
                } catch (NoNextQuotationAvailableException e) {
                    Log.w(LOG_TAG, e.getMessage());
                }
            }
        }
    }

    @Override
    public void onDeleted(final Context context, final int[] widgetIds) {
        // a widget instance is removed from the home screen
        super.onDeleted(context, widgetIds);

        for (final int widgetId : widgetIds) {
            Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                    new Object() {
                    }.getClass().getEnclosingMethod().getName()));

            getQuoteUnquoteModelInstance(context).removeDatabaseEntriesForInstance(widgetId);
            Preferences.removeSharedPreferencesForWidgetId(context, widgetId);

            final DailyAlarm dailyAlarm = new DailyAlarm(context, widgetId);
            dailyAlarm.resetAnyExistingDailyAlarm();
        }
    }

    @Override
    public void onDisabled(final Context context) {
        // last widget instance deleted
        super.onDisabled(context);

        Log.d(LOG_TAG, String.format("%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        try {
            getQuoteUnquoteModelInstance(context).removeDatabaseEntriesForAllInstances();
            Preferences.empty(context);

            if (CloudServiceSend.isRunning(context)) {
                context.stopService(new Intent(context, CloudServiceSend.class));
            }
        } finally {
            shutdownQuoteUnquoteModel(context);
        }
    }
}
