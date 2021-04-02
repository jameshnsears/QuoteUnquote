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
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
import com.github.jameshnsears.quoteunquote.report.ReportActivity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.NotificationHelper;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public final class QuoteUnquoteWidget extends AppWidgetProvider {
    @Nullable
    private static ExecutorService executorService;
    private static volatile boolean receiversRegistered = false;
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @NonNull
    private final NotificationHelper notificationHelper = new NotificationHelper();

    private static void registerReceivers(@NonNull Context contextIn) {
        Timber.d("receiversRegistered=%b", receiversRegistered);

        if (!receiversRegistered) {
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
    }

    @Override
    public void onEnabled(@NonNull final Context context) {
        final ContentPreferences contentPreferences = new ContentPreferences(context);
        contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());
        startDatabaseConnectivity(context);
    }

    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(5);
        }
        Timber.d(executorService.toString());
        return executorService;
    }

    public static void stopExecutorService() {
        if (executorService != null) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5000, TimeUnit.MICROSECONDS)) {
                        Timber.d("awaitTermination=timeout");
                    }
                } catch (@NonNull InterruptedException e) {
                    Timber.e(e);
                    Thread.currentThread().interrupt();
                }
                Timber.d(executorService.toString());
            }));
        }
    }

    @Override
    public void onUpdate(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] widgetIds) {
        registerReceivers(context);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

        for (final int widgetId : widgetIds) {
            Timber.d("%d", widgetId);

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

            if (widgetId != 0) {
                setTransparency(context, widgetId, remoteViews);

                setToolbarButtonsVisibility(context, widgetId, remoteViews);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        // at end, so that onReceive get's called first
        super.onUpdate(context, appWidgetManager, widgetIds);
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
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    onReceiveMyPackageReplaced(context, widgetId, appWidgetManager);
                    break;

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
                    onReceiveActivityFinishedConfiguration(context, widgetId, eventDailyAlarm);
                    break;

                case IntentFactoryHelper.ACTIVITY_FINISHED_REPORT:
                    onReceiveActivityFinishedReport(widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    onReceiveDailyAlarm(context, widgetId, eventDailyAlarm);
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
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)
                    || !intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveMyPackageReplaced(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        Timber.d("performing update: %d", widgetId);

        // adb uninstall com.github.jameshnsears.quoteunquote
        //
        // adb install -r ~/Desktop/app-debug-1.0.1.apk
        //
        // Build > Build Bundle(s) / APK(s) > Build APK(s)
        // adb install -r app/build/outputs/apk/googleplay/debug/app-googleplay-debug.apk
        //
        // look at Logcat
        stopDatabaseConnectivity();
        stopExecutorService();

        DatabaseRepository.resetDatabaseInstances(context);

        startDatabaseConnectivity(context);

        // < version 2.0.0
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);
        appearancePreferences.performMigration();

        ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);
        contentPreferences.performMigration();

        EventPreferences eventPreferences = new EventPreferences(widgetId, context);
        eventPreferences.performMigration();

        for (int id: appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            Timber.d("%d", id);
            getQuoteUnquoteModel(context).resetPrevious(id, new ContentPreferences(id, context).getContentSelection());
            getQuoteUnquoteModel(context).markAsCurrentDefault(id);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.listViewQuotation);
        }
    }

    private void startDatabaseConnectivity(@NonNull Context context) {
        setQuoteUnquoteModel(new QuoteUnquoteModel(context));
    }

    public void stopDatabaseConnectivity() {
        quoteUnquoteModel = null;
    }

    private void onReceiveActivityFinishedReport(
            final int widgetId, @NonNull final AppWidgetManager appWidgetManager) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDeviceUnlock(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager) {
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));

        for (final int widgetId : widgetIds) {
            if (new EventPreferences(widgetId, context).getEventDeviceUnlock()) {
                scheduleEvent(context, widgetId);
            }
        }

        onUpdate(context, appWidgetManager, widgetIds);
    }

    public void onReceiveToolbarPressedShare(@NonNull final Context context, final int widgetId) {
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                getQuoteUnquoteModel(context).getCurrentQuotation(widgetId).theQuotation()));
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        QuotationEntity currentQuotation = getQuoteUnquoteModel(context).getCurrentQuotation(
                widgetId);

        int favouritesCount = getQuoteUnquoteModel(context).toggleFavourite(
                widgetId, currentQuotation.digest);

        if (contentPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
            if (favouritesCount == 0) {
                noFavouritesSoMoveToAll(context, widgetId, contentPreferences);
            } else {
                getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void noFavouritesSoMoveToAll(
            @NonNull Context context,
            int widgetId,
            @NonNull ContentPreferences contentPreferences) {
        Timber.d("%s", contentPreferences.getContentSelection());

        if (contentPreferences.getContentSelection() != ContentSelection.ALL) {
            contentPreferences.setContentSelection(ContentSelection.ALL);
            Timber.d("%s", contentPreferences.getContentSelection());
            getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        }
    }

    public void onReceiveToolbarPressedFirst(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(context).resetPrevious(widgetId, new ContentPreferences(widgetId, context).getContentSelection());
        getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedPrevious(
            @NonNull final Context context, final int widgetId, @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(context).markAsCurrentPrevious(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
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
        getQuoteUnquoteModel(context).markAsCurrentNext(widgetId, randomNext);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDailyAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final EventDailyAlarm scheduleDailyAlarm) {
        scheduleDailyAlarm.setDailyAlarm();
        scheduleEvent(context, widgetId);
    }

    private void scheduleEvent(@NonNull Context context, int widgetId) {
        EventPreferences eventPreferences = new EventPreferences(widgetId, context);

        getQuoteUnquoteModel(context).markAsCurrentNext(widgetId, eventPreferences.getEventNextRandom());

        if (eventPreferences.getEventDisplayWidgetAndNotification()) {
            QuotationEntity currentQuotation = getQuoteUnquoteModel(context).getCurrentQuotation(
                    widgetId);

            notificationHelper.displayNotification(context, currentQuotation);
        }
    }

    private void onReceiveActivityFinishedConfiguration(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final EventDailyAlarm eventDailyAlarm) {
        getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        eventDailyAlarm.setDailyAlarm();
    }

    private void setTransparency(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        final int seekBarValue = new AppearancePreferences(widgetId, context).getAppearanceTransparency();
        Timber.d("%d", widgetId);

        final String setBackgroundColor = "setBackgroundColor";
        remoteViews.setInt(R.id.listViewQuotation, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonFirst, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonPrevious, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonReport, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonFavourite, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonShare, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonNextRandom, setBackgroundColor, getTransparencyMask(seekBarValue));
        remoteViews.setInt(R.id.imageButtonNextSequential, setBackgroundColor, getTransparencyMask(seekBarValue));
    }

    private int getTransparencyMask(final int seekBarValue) {
        float transparency = 1;
        if (seekBarValue != -1) {
            transparency -= seekBarValue * .1f;
        }

        return (int) (transparency * 0xFF) << 24 | 0xFFFFFF;
    }

    private void setToolbarButtonsVisibility(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

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

    public void setHeartColour(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        final QuotationEntity quotationEntity = getQuoteUnquoteModel(context).getCurrentQuotation(
                widgetId);

        if (quotationEntity != null) {
            // null check needed for startup
            if (getQuoteUnquoteModel(context).isFavourite(quotationEntity.digest)) {
                remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_red_24dp);
            } else {
                remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    @Override
    public void onDeleted(
            @NonNull final Context context,
            @NonNull final int[] widgetIds) {
        // a widget instance deleted
        super.onDeleted(context, widgetIds);

        for (final int widgetId : widgetIds) {
            Timber.d("%d", widgetId);

            getQuoteUnquoteModel(context).delete(widgetId);
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
            getQuoteUnquoteModel(context).disable();
            PreferencesFacade.disable(context);

            if (CloudServiceSend.isRunning) {
                context.stopService(new Intent(context, CloudServiceSend.class));
            }
        } finally {
            stopDatabaseConnectivity();
            stopExecutorService();
        }
    }

    @Nullable
    public QuoteUnquoteModel getQuoteUnquoteModel(@NonNull final Context context) {
        if (quoteUnquoteModel == null) {
            quoteUnquoteModel = new QuoteUnquoteModel(context);
        }
        return quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }
}
