package com.github.jameshnsears.quoteunquote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceSend;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventDailyAlarm;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventPreferences;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
import com.github.jameshnsears.quoteunquote.report.ReportActivity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.NotificationHelper;
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import timber.log.Timber;

public final class QuoteUnquoteWidget extends AppWidgetProvider {
    private static volatile boolean receiversRegistered = false;
    @Nullable
    private QuoteUnquoteModel quoteUnquoteModel;
    private NotificationHelper notificationHelper = new NotificationHelper();

    private static void registerReceivers(@NonNull Context contextIn) {
        if (receiversRegistered) {
            return;
        }

        Context context = contextIn.getApplicationContext();
        QuoteUnquoteWidget receiver = new QuoteUnquoteWidget();

        IntentFilter userPresent = new IntentFilter();
        userPresent.addAction("android.intent.action.USER_PRESENT");
        context.registerReceiver(receiver, userPresent);

        IntentFilter bootCompleted = new IntentFilter();
        bootCompleted.addAction("android.intent.action.BOOT_COMPLETED");
        context.registerReceiver(receiver, bootCompleted);

        IntentFilter quickBootPowerOn = new IntentFilter();
        quickBootPowerOn.addAction("android.intent.action.QUICKBOOT_POWERON");
        context.registerReceiver(receiver, quickBootPowerOn);

        receiversRegistered = true;
    }

    @NonNull
    public synchronized QuoteUnquoteModel getQuoteUnquoteModelInstance(@NonNull final Context context) {
        if (quoteUnquoteModel == null) {
            quoteUnquoteModel = new QuoteUnquoteModel(context);
        }

        return quoteUnquoteModel;
    }

    public void shutdownQuoteUnquoteModel(@NonNull final Context context) {
        if (quoteUnquoteModel != null) {
            getQuoteUnquoteModelInstance(context).shutdown();
            quoteUnquoteModel = null;
        }
    }

    @Override
    public void onEnabled(@NonNull final Context context) {
        final ContentPreferences contentPreferences = new ContentPreferences(context);
        contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());

        if (BuildConfig.DEBUG && Timber.treeCount() == 0) {
            Timber.plant(new MethodLineLoggingTree());
        }

        Timber.d("onEnabled");
    }

    @Override
    public void onUpdate(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] widgetIds) {
        registerReceivers(context);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

        for (final int widgetId : widgetIds) {

            remoteViews.setRemoteAdapter(
                    R.id.listViewQuotation,
                    IntentFactoryHelper.createIntent(context, ListViewService.class, widgetId));

            remoteViews.setPendingIntentTemplate(
                    R.id.listViewQuotation,
                    PendingIntent.getActivity(
                            context,
                            widgetId,
                            IntentFactoryHelper.createIntent(context, ConfigureActivity.class, widgetId),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFirst,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FIRST));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonPrevious,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonReport,
                    PendingIntent.getActivity(
                            context,
                            widgetId,
                            IntentFactoryHelper.createIntent(context, ReportActivity.class, widgetId),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFavourite,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonShare,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_SHARE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNextRandom,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNextSequential,
                    IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL));

            setTransparency(context, widgetId, remoteViews);

            setToolbarButtonsVisibility(context, widgetId, remoteViews);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);

        final int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Timber.d("%d: action=%s", widgetId, intent.getAction());

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        final EventDailyAlarm eventDailyAlarm = new EventDailyAlarm(context, widgetId);

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
                    onReceiveActivityFinishedConfiguration(widgetId, appWidgetManager, eventDailyAlarm);
                    break;

                case IntentFactoryHelper.ACTIVITY_FINISHED_REPORT:
                    onReceiveActivityFinishedReport(widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    onReceiveDailyAlarm(context, widgetId, appWidgetManager, eventDailyAlarm);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FIRST:
                    onReceiveToolbarPressedFirst(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS:
                    onReceiveToolbarPressedPrevious(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE:
                    onReceiveToolbarPressedFavourite(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_SHARE:
                    onReceiveToolbarPressedShare(context, widgetId);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM:
                    onReceiveToolbarPressedNextRandom(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL:
                    onReceiveToolbarPressedNextSequential(context, widgetId, appWidgetManager);
                    break;

                default:
                    break;
            }
        } finally {
            // mainly for screen rotation!
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveActivityFinishedReport(
            final int widgetId, @NonNull final AppWidgetManager appWidgetManager) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDeviceUnlock(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager) {
        unlockDevice(context, appWidgetManager);
    }

    private void onReceiveToolbarPressedShare(@NonNull final Context context, final int widgetId) {
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                getQuoteUnquoteModelInstance(context).getNext(
                        widgetId, new ContentPreferences(widgetId, context).getContentSelection()).theQuotation()));
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        getQuoteUnquoteModelInstance(context).toggleFavourite(widgetId, getQuoteUnquoteModelInstance(context).getNext(
                widgetId, contentPreferences.getContentSelection()).digest);

        toggleFavouriteColour(context, widgetId, appWidgetManager);

        if (contentPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)
                || contentPreferences.getContentSelection().equals(ContentSelection.ALL)) {
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void onReceiveToolbarPressedFirst(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        ToastHelper.makeToast(context, context.getString(R.string.widget_button_first_toast), Toast.LENGTH_LONG);

        getQuoteUnquoteModelInstance(context).resetPrevious(widgetId, new ContentPreferences(widgetId, context).getContentSelection());

        updateWidgetView(context, widgetId, appWidgetManager);
    }

    private void onReceiveToolbarPressedPrevious(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager) {
        updateWidgetView(context, widgetId, appWidgetManager);
    }

    private void onReceiveToolbarPressedNextRandom(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        onReceiveToolbarPressedNext(context, widgetId, appWidgetManager, true);
    }

    private void onReceiveToolbarPressedNextSequential(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        onReceiveToolbarPressedNext(context, widgetId, appWidgetManager, false);
    }

    private void onReceiveToolbarPressedNext(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            final boolean randomNext) {
        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        try {
            getQuoteUnquoteModelInstance(context).setNext(widgetId, contentPreferences.getContentSelection(), randomNext);

            updateWidgetView(context, widgetId, appWidgetManager);
        } catch (NoNextQuotationAvailableException e) {
            ToastHelper.makeToast(context, context.getString(R.string.widget_button_next_toast), Toast.LENGTH_LONG);
        }
    }

    private void unlockDevice(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager) {
        for (final int widgetId : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            if (new EventPreferences(widgetId, context).getEventDeviceUnlock()) {
                try {
                    scheduleEvent(context, widgetId, appWidgetManager);
                } catch (NoNextQuotationAvailableException e) {
                    Timber.d(e);
                }
            }
        }
    }

    private void onReceiveDailyAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final EventDailyAlarm eventDailyAlarm) {
        eventDailyAlarm.setDailyAlarm();
        try {
            scheduleEvent(context, widgetId, appWidgetManager);
        } catch (NoNextQuotationAvailableException e) {
            Timber.d(e);
        }
    }

    private void scheduleEvent(@NonNull Context context, int widgetId, @NonNull AppWidgetManager appWidgetManager) throws NoNextQuotationAvailableException {
        ContentSelection contentSelection = new ContentPreferences(widgetId, context).getContentSelection();
        EventPreferences eventPreferences = new EventPreferences(widgetId, context);

        try {
            getQuoteUnquoteModelInstance(context).setNext(widgetId, contentSelection, eventPreferences.getEventNextRandom());

            if (eventPreferences.getEventDisplayWidgetAndNotification()) {
                notificationHelper.displayNotification(
                        widgetId, context, getQuoteUnquoteModelInstance(context).getNext(widgetId, contentSelection));
            }
        } catch (NoNextQuotationAvailableException e) {
            ToastHelper.makeToast(context, context.getString(R.string.widget_button_next_toast), Toast.LENGTH_LONG);
        }
        updateWidgetView(context, widgetId, appWidgetManager);
    }

    private void updateWidgetView(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        toggleFavouriteColour(context, widgetId, appWidgetManager);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveActivityFinishedConfiguration(
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final EventDailyAlarm eventDailyAlarm) {
        eventDailyAlarm.setDailyAlarm();
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void setTransparency(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        logWidgetId(widgetId);

        final int seekBarValue = new AppearancePreferences(widgetId, context).getAppearanceTransparency();
        Timber.d("seekBarValue=%d", seekBarValue);

        final String setBackgroundColor = "setBackgroundColor";
        remoteViews.setInt(R.id.listViewQuotation, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonFirst, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonPrevious, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonReport, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonFavourite, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonShare, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonNextRandom, setBackgroundColor, transparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonNextSequential, setBackgroundColor, transparencyMask(seekBarValue));
    }

    private int transparencyMask(final int seekBarValue) {
        float transparency = 1;
        if (seekBarValue != -1) {
            transparency -= seekBarValue * .1f;
        }

        return (int) (transparency * 0xFF) << 24 | 0xFFFFFF;
    }

    private void logWidgetId(final int widgetId) {
        Timber.d("widgetId=%d", widgetId);
    }

    private void setToolbarButtonsVisibility(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        logWidgetId(widgetId);

        final AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarFirst(),
                R.id.imageButtonFirst);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarPrevious(),
                R.id.imageButtonPrevious);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarReport(),
                R.id.imageButtonReport);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarFavourite(),
                R.id.imageButtonFavourite);

        setHeartColour(context, widgetId, remoteViews);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarShare(),
                R.id.imageButtonShare);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarRandom(),
                R.id.imageButtonNextRandom);

        setToolbarButtonVisibility(
                remoteViews,
                appearancePreferences.getAppearanceToolbarSequential(),
                R.id.imageButtonNextSequential);
    }

    private void setToolbarButtonVisibility(
            @NonNull final RemoteViews remoteViews,
            final boolean toolbarButtonEnabled,
            @IdRes final int imageButtonId) {
        if (toolbarButtonEnabled) {
            remoteViews.setViewVisibility(imageButtonId, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(imageButtonId, View.GONE);
        }
    }

    private void toggleFavouriteColour(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        logWidgetId(widgetId);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

        setHeartColour(context, widgetId, remoteViews);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    private void setHeartColour(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        final QuotationEntity quotationEntity
                = getQuoteUnquoteModelInstance(context).getNext(widgetId, new ContentPreferences(widgetId, context).getContentSelection());

        if (getQuoteUnquoteModelInstance(context).isFavourite(widgetId, quotationEntity.digest)) {
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_red_24dp);
        } else {
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_border_black_24dp);
        }
    }

    @Override
    public void onDeleted(
            @NonNull final Context context,
            @NonNull final int[] widgetIds) {
        // a widget instance is removed from the home screen
        super.onDeleted(context, widgetIds);

        for (final int widgetId : widgetIds) {
            logWidgetId(widgetId);

            getQuoteUnquoteModelInstance(context).delete(widgetId);
            PreferencesFacade.delete(context, widgetId);

            final EventDailyAlarm eventDailyAlarm = new EventDailyAlarm(context, widgetId);
            eventDailyAlarm.resetAnyExistingDailyAlarm();
        }
    }

    @Override
    public void onDisabled(@NonNull final Context context) {
        // last widget instance deleted
        super.onDisabled(context);

        try {
            getQuoteUnquoteModelInstance(context).disable();
            PreferencesFacade.disable(context);

            if (CloudServiceSend.isRunning) {
                context.stopService(new Intent(context, CloudServiceSend.class));
            }
        } finally {
            shutdownQuoteUnquoteModel(context);
        }
    }
}
