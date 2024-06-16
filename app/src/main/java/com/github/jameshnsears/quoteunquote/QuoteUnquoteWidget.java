package com.github.jameshnsears.quoteunquote;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.github.jameshnsears.quoteunquote.cloud.CloudService;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceBackup;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceRestore;
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationContent;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationCoordinator;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationEvent;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationHelper;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationsCustomisableIntervalAlarm;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationsDailyAlarm;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperAlarm;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperData;
import com.github.jameshnsears.quoteunquote.utils.sync.AutoCloudBackupAlarm;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class QuoteUnquoteWidget extends AppWidgetProvider {
    @Nullable
    public static ContentSelection currentContentSelection = ContentSelection.ALL;
    @Nullable
    public static String currentAuthorSelection;
    @Nullable
    public static int notificationPermissionDeniedCount = 0;
    @Nullable
    private static ExecutorService executorService;
    private static volatile boolean receiversRegistered;
    @Nullable
    private static NotificationHelper notificationHelper;
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    private NotificationCoordinator notificationCoordinator;

    @SuppressLint({"UnspecifiedRegisterReceiverFlag", "InlinedApi"})
    private static void registerReceivers(@NonNull Context contextIn) {
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.registerReceiver(receiver, quickBootPowerOn);
            } else {
                context.registerReceiver(receiver, quickBootPowerOn, Context.RECEIVER_EXPORTED);
            }

            IntentFilter themeChange = new IntentFilter();
            themeChange.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            context.registerReceiver(receiver, themeChange);

            receiversRegistered = true;
        }
    }

    @Nullable
    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(
                    10,
                    new ThreadFactoryBuilder().setNameFormat("QuoteUnquote-thread-%d").build());
        }
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

    private static void setAutoCloudBackupAlarm(@NonNull Context context, int widgetId) {
        SyncPreferences syncPreferences = new SyncPreferences(widgetId, context);
        if (syncPreferences.getAutoCloudBackup()) {
            Timber.d("autoCloudBackupAlarm.setAutoCloudBackupAlarm");
            AutoCloudBackupAlarm autoCloudBackupAlarm = new AutoCloudBackupAlarm(context, widgetId);
            autoCloudBackupAlarm.resetAlarm();
            autoCloudBackupAlarm.setAlarm();
        }
    }

    @NonNull
    public QuotationsPreferences getQuotationsPreferences(Context context, int widgetId) {
        return new QuotationsPreferences(widgetId, context);
    }

    @Override
    public void onEnabled(@NonNull final Context context) {
        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(context);
        if (quotationsPreferences.getContentLocalCode().equals("")) {
            Timber.d("setting LocalCode");
            quotationsPreferences.setContentLocalCode(CloudTransferHelper.getLocalCode());
        }

        startDatabaseConnectivity(-1, context);
    }

    @Override
    public void onUpdate(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] widgetIds) {
        registerReceivers(context);

        for (final int widgetId : widgetIds) {
            Timber.d("onUpdate: widgetId=%d", widgetId);

            final RemoteViews remoteViews
                    = new RemoteViews(context.getPackageName(), getWidgetLayout(widgetId, context));

            remoteViews.setRemoteAdapter(
                    R.id.listViewQuotation,
                    IntentFactoryHelper.createIntent(context, ListViewService.class, widgetId));

            remoteViews.setPendingIntentTemplate(R.id.listViewQuotation,
                    IntentFactoryHelper.createPendingIntentTemplate(context));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFirst,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FIRST));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonPrevious,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonFavourite,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonShare,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_SHARE));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonJump,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_JUMP));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNextRandom,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNextSequential,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL));

            if (widgetId != 0) {
                AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

                if (!appearancePreferences.getAppearanceForceFollowSystemTheme()) {
                    setTransparency(context, widgetId, remoteViews);

                    setToolbarButtons(
                            context,
                            widgetId,
                            remoteViews,
                            appearancePreferences.getAppearanceToolbarColour()
                    );
                } else {
                    setThemeToolbarButtons(context, widgetId, remoteViews);
                }
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, widgetIds);
    }

    private NotificationHelper getNotificationHelper(@NonNull Context context) {
        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(context);
        }

        return notificationHelper;
    }

    private NotificationCoordinator getNotificationCoordinator(@NonNull Context context) {
        if (notificationCoordinator == null) {
            notificationCoordinator = new NotificationCoordinator();
        }

        return notificationCoordinator;
    }

    private int getWidgetLayout(int widgetId, @NonNull Context context) {
        int layout = R.layout.quote_unquote_widget;

        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);
        boolean isToolbarVisible = isToolbarVisible(widgetId, appearancePreferences);

        if (!isToolbarVisible) {
            layout = R.layout.quote_unquote_widget_no_toolbar;
        }

        if (appearancePreferences.getAppearanceForceFollowSystemTheme() == true) {
            if (isNightMode(context)) {
                Timber.d("Theme: layout, night");
                layout = R.layout.quote_unquote_widget_system_theme_night;
                if (!isToolbarVisible) {
                    layout = R.layout.quote_unquote_widget_system_theme_night_no_toolbar;
                }
            } else {
                Timber.d("Theme: layout, day");
                layout = R.layout.quote_unquote_widget_system_theme_day;
                if (!isToolbarVisible) {
                    layout = R.layout.quote_unquote_widget_system_theme_day_no_toolbar;
                }
            }
        }

        return layout;
    }

    private boolean isToolbarVisible(int widgetId, AppearancePreferences appearancePreferences) {
        if (widgetId != 0
                &&
                !appearancePreferences.getAppearanceToolbarFirst()
                &&
                !appearancePreferences.getAppearanceToolbarPrevious()
                &&
                !appearancePreferences.getAppearanceToolbarFavourite()
                &&
                !appearancePreferences.getAppearanceToolbarShare()
                &&
                !appearancePreferences.getAppearanceToolbarJump()
                &&
                !appearancePreferences.getAppearanceToolbarSequential()
                &&
                !appearancePreferences.getAppearanceToolbarRandom()
        ) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);

        final int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Timber.d("widgetId=%d; action=%s", widgetId, intent.getAction());

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        try {
            final NotificationsDailyAlarm notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);
            final NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm = new NotificationsCustomisableIntervalAlarm(context, widgetId);
            final ScraperAlarm scraperAlarm = new ScraperAlarm(context, widgetId);
            final AutoCloudBackupAlarm autoCloudBackupAlarm = new AutoCloudBackupAlarm(context, widgetId);

            switch (intent.getAction()) {
                case Intent.ACTION_CONFIGURATION_CHANGED:
                    onReceiveThemeChange(context, appWidgetManager);
                    break;

                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    onReceiveMyPackageReplaced(context, appWidgetManager);
                    break;

                case Intent.ACTION_USER_PRESENT:
                    startDatabaseConnectivity(widgetId, context);
                    onReceiveDeviceUnlock(context, appWidgetManager);
                    break;

                case IntentFactoryHelper.NOTIFICATION_FAVOURITE_PRESSED:
                    onReceiveNotificationFavourite(context, intent, appWidgetManager);
                    break;

                case IntentFactoryHelper.NOTIFICATION_NEXT_PRESSED:
                    onReceiveNotificationNext(context, intent, appWidgetManager);
                    break;

                case IntentFactoryHelper.NOTIFICATION_DISMISSED:
                    onReceiveNotificationDismissed(context, intent);
                    break;

                /*
                adb shell
                am broadcast -a android.intent.action.BOOT_COMPLETED
                */
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_REBOOT:
                case IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION:
                    onReceiveActivityFinishedConfiguration(
                            context,
                            widgetId,
                            notificationsDailyAlarm,
                            notificationsCustomisableIntervalAlarm,
                            autoCloudBackupAlarm,
                            scraperAlarm);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    onReceiveDailyAlarm(context, widgetId, notificationsDailyAlarm);
                    break;

                case IntentFactoryHelper.CUSTOMISABLE_INTERVAL_ALARM:
                    onReceiveCustomisableIntervalAlarm(context, widgetId, notificationsCustomisableIntervalAlarm);
                    break;

                case IntentFactoryHelper.AUTO_CLOUD_BACKUP_ALARM:
                    onReceiveAutoCloudBackupAlarm(context, widgetId, autoCloudBackupAlarm);
                    break;

                case IntentFactoryHelper.SCRAPER_ALARM:
                    onReceiveScraperAlarm(context, widgetId, appWidgetManager, scraperAlarm);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FIRST:
                    onReceiveToolbarPressedFirst(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS:
                    onReceiveToolbarPressedPrevious(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE:
                    onReceiveToolbarPressedFavourite(
                            context,
                            widgetId,
                            getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId).digest,
                            appWidgetManager);
                    break;

                case IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION:
                    onReceiveAllWidgetInstancesFavouriteNotification(context, widgetId, intent, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_SHARE:
                    onReceiveToolbarPressedShare(context, widgetId);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_JUMP:
                    onReceiveToolbarPressedJump(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM:
                    onReceiveToolbarPressedNextRandom(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL:
                    onReceiveToolbarPressedNextSequential(context, widgetId, appWidgetManager);
                    break;

                case AppWidgetManager.ACTION_APPWIDGET_ENABLED:
                    onReceiveActionAppwidgetEnabled(context, appWidgetManager);
                    break;

                default:
                    break;
            }
        } catch (NullPointerException e) {
            Timber.e("%s", e.getMessage());
        } finally {
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void setThemeToolbarButtons(
            @NonNull Context context,
            int widgetId,
            @NonNull RemoteViews remoteViews
    ) {
        if (isNightMode(context)) {
            setToolbarButtons(context, widgetId, remoteViews, "#FFFFFFFF");
        } else {
            setToolbarButtons(context, widgetId, remoteViews, "#FF000000");
        }
    }

    private boolean isNightMode(@NonNull Context context) {
        boolean isNightMode = false;

        if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true;
        }

        return isNightMode;
    }

    private void onReceiveThemeChange(@NonNull Context context,
                                      @NonNull final AppWidgetManager appWidgetManager) {
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));

        onUpdate(context, appWidgetManager, widgetIds);

        for (final int knownWidgetId : widgetIds) {
            appWidgetManager.notifyAppWidgetViewDataChanged(knownWidgetId, R.id.listViewQuotation);
        }
    }

    private void onReceiveMyPackageReplaced(@NonNull Context context,
                                            @NonNull final AppWidgetManager appWidgetManager) {
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));
        for (final int knownWidgetId : widgetIds) {
            Timber.d("onReceiveMyPackageReplaced: %d", knownWidgetId);

            getQuoteUnquoteModel(knownWidgetId, context).alignHistoryWithQuotations(knownWidgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(knownWidgetId, R.id.listViewQuotation);
        }
    }

    private void onReceiveActionAppwidgetEnabled(@NonNull final Context context,
                                                 @NonNull final AppWidgetManager appWidgetManager) {
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));
        for (final int widgetId : widgetIds) {
            Timber.d("setDailyAlarm: %d", widgetId);
            NotificationsDailyAlarm notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);
            notificationsDailyAlarm.setAlarm();

            Timber.d("setBihourlyAlarm: %d", widgetId);
            NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm = new NotificationsCustomisableIntervalAlarm(context, widgetId);
            notificationsCustomisableIntervalAlarm.setAlarm();

            Timber.d("scraperAlarm: %d", widgetId);
            ScraperAlarm scraperAlarm = new ScraperAlarm(context, widgetId);
            scraperAlarm.setAlarm();
        }
    }

    private void onReceiveAllWidgetInstancesFavouriteNotification(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final Intent intent,
            final AppWidgetManager appWidgetManager) {

        String digest = intent.getExtras().getString("digest");
        int notificationId = intent.getExtras().getInt("notificationId");
        Timber.d("digest=%s; notificationId=%d", digest, notificationId);

        if (getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId).digest.equals(digest)) {
            setHeartColour(
                    context,
                    widgetId,
                    new RemoteViews(context.getPackageName(), getWidgetLayout(widgetId, context)));

            final QuotationsPreferences quotationsPreferences = getQuotationsPreferences(context, widgetId);

            int favouritesCount = getQuoteUnquoteModel(widgetId, context).countFavouritesWithoutRx();

            if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
                if (favouritesCount == 0) {
                    noFavouritesSoMoveToAll(context, widgetId, quotationsPreferences);
                } else {
                    getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
                }

                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
            }
        }

        setAutoCloudBackupAlarm(context, widgetId);
    }

    private void startDatabaseConnectivity(int widgetId, @NonNull Context context) {
        setQuoteUnquoteModel(new QuoteUnquoteModel(widgetId, context));
    }

    public void stopDatabaseConnectivity() {
        quoteUnquoteModel = null;
    }

    private void onReceiveDeviceUnlock(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager) {

        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));

        for (final int widgetId : widgetIds) {
            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);
            if (notificationsPreferences.getEventDeviceUnlock()) {
                scheduleEvent(context, widgetId, NotificationEvent.DEVICE_UNLOCK);
            }
        }

        onUpdate(context, appWidgetManager, widgetIds);
    }

    public void onReceiveToolbarPressedShare(@NonNull final Context context, final int widgetId) {
        final AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        String appName = context.getResources().getString(R.string.app_name);

        QuotationEntity currentQuotation = getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId);

        if (appearancePreferences.getAppearanceToolbarShareNoSource()) {
            context.startActivity(IntentFactoryHelper.createIntentShare(
                    appName, currentQuotation.shareQuotation()));
        } else {
            context.startActivity(IntentFactoryHelper.createIntentShare(
                    appName, currentQuotation.shareQuotationAuthor()));
        }
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull final Context context,
            final int widgetId,
            @NonNull String digest,
            @NonNull final AppWidgetManager appWidgetManager) {

        updateWidgetFavourite(context, widgetId, digest, appWidgetManager);

        if (getNotificationCoordinator(context).isNotificationShowingQuotation(digest)) {
            updateNotificationFavourite(
                    context,
                    widgetId,
                    digest);
        }

        setAutoCloudBackupAlarm(context, widgetId);
    }

    private void updateWidgetFavourite(
            @NonNull Context context,
            int widgetId,
            @NonNull String digest,
            @NonNull AppWidgetManager appWidgetManager) {
        final QuotationsPreferences quotationsPreferences = getQuotationsPreferences(context, widgetId);

        int favouritesCount = getQuoteUnquoteModel(widgetId, context).toggleFavourite(widgetId, digest);

        if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES
                || (quotationsPreferences.getContentSelection() == ContentSelection.SEARCH
                && quotationsPreferences.getContentSelectionSearchFavouritesOnly() == true)) {
            if (favouritesCount == 0) {
                noFavouritesSoMoveToAll(context, widgetId, quotationsPreferences);
            } else {
                getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }

        sendAllWidgetInstancesFavouriteNotification(context, widgetId, digest, appWidgetManager);
    }

    private void sendAllWidgetInstancesFavouriteNotification(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final String digest,
            @NonNull final AppWidgetManager appWidgetManager) {
        for (int id : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            if (id != widgetId) {
                Timber.d("from=%d; to=%d", widgetId, id);
                final Intent instancesIntent = IntentFactoryHelper.createIntent(context, id);
                instancesIntent.setAction(IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION);

                Bundle bundle = new Bundle();
                bundle.putString("digest", digest);
                instancesIntent.putExtras(bundle);

                context.sendBroadcast(instancesIntent);
            }
        }
    }

    private void noFavouritesSoMoveToAll(
            @NonNull Context context,
            int widgetId,
            @NonNull QuotationsPreferences quotationsPreferences) {
        Timber.d("%s", quotationsPreferences.getContentSelection());

        if (quotationsPreferences.getContentSelection() != ContentSelection.ALL) {
            quotationsPreferences.setContentSelection(ContentSelection.ALL);
            Timber.d("%s", quotationsPreferences.getContentSelection());
            getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
        }
    }

    public void onReceiveToolbarPressedFirst(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(widgetId, context).resetPrevious(widgetId, getQuotationsPreferences(context, widgetId).getContentSelection());
        getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedPrevious(
            @NonNull final Context context, final int widgetId, @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(widgetId, context).markAsCurrentPrevious(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedNextRandom(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        onReceiveToolbarPressedNext(context, widgetId, appWidgetManager, true);
    }

    public void onReceiveToolbarPressedJump(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        Timber.d("widgetId=%d", widgetId);
        getQuoteUnquoteModel(widgetId, context).markAsCurrentLastPrevious(widgetId);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
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
        getQuoteUnquoteModel(widgetId, context).markAsCurrentNext(widgetId, randomNext);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDailyAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final NotificationsDailyAlarm notificationsDailyAlarm) {
        // reschedule, as recurring
        notificationsDailyAlarm.setAlarm();
        scheduleEvent(context, widgetId, NotificationEvent.EVENT_DAILY);
    }

    private void onReceiveCustomisableIntervalAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm) {
        Timber.d("customisableIntervalAlarm");
        notificationsCustomisableIntervalAlarm.setAlarm();
        scheduleEvent(context, widgetId, NotificationEvent.CUSTOMISABLE_INTERVAL);
    }

    private void onReceiveAutoCloudBackupAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AutoCloudBackupAlarm autoCloudBackupAlarm) {
        Timber.d("autoCloudBackupAlarm.onReceiveAutoCloudBackupAlarm");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                SyncPreferences syncPreferences = new SyncPreferences(widgetId, context);
                syncPreferences.setAutoCloudBackup(false);
                return;
            }
        }

        getQuoteUnquoteModel(widgetId, context).autoClobuBackup(widgetId, autoCloudBackupAlarm);
    }

    private void onReceiveScraperAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final ScraperAlarm scraperAlarm) {
        scraperAlarm.setAlarm();

        QuotationsPreferences quotationsPreferences = getQuotationsPreferences(context, widgetId);

        ScraperData scraperData = getQuoteUnquoteModel(widgetId, context).getWebPage(
                context,
                quotationsPreferences.getDatabaseWebUrl(),
                quotationsPreferences.getDatabaseWebXpathQuotation(),
                quotationsPreferences.getDatabaseWebXpathSource()
        );

        if (scraperData.getScrapeResult()) {
            displayAppropriateScrapedQuotation(
                    context,
                    widgetId,
                    scraperData.getQuotation(),
                    scraperData.getSource() // + " : " + Calendar.getInstance().get(Calendar.MINUTE);
            );

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    public void displayAppropriateScrapedQuotation(
            @NonNull Context context,
            int widgetId,
            @NonNull String quotation,
            @NonNull String source) {

        if (getQuotationsPreferences(context, widgetId).getDatabaseWebKeepLatestOnly()) {
            Timber.w("scraper: keep latest only");
            getQuoteUnquoteModel(widgetId, context).insertWebPage(
                    widgetId,
                    quotation,
                    source,
                    ImportHelper.DEFAULT_DIGEST
            );
            return;
        }

        QuotationEntity currentQuotation
                = getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId);
        if (currentQuotation.quotation.equals(quotation)
                && currentQuotation.author.equals(source)) {
            Timber.w("scraper: current");
            return;
        }

        QuotationEntity defaultQuotation
                = getQuoteUnquoteModel(widgetId, context).getQuotation(ImportHelper.DEFAULT_DIGEST);
        if (defaultQuotation != null
                && defaultQuotation.quotation.equals(quotation)
                && defaultQuotation.author.equals(source)) {
            Timber.w("scraper: previous, default");
            getQuoteUnquoteModel(widgetId, context).markAsCurrent(widgetId, ImportHelper.DEFAULT_DIGEST);
            return;
        }

        String digest = ImportHelper.makeDigest(quotation, source);
        if (getQuoteUnquoteModel(widgetId, context).getQuotation(digest) != null) {
            Timber.w("scraper: previous");
            getQuoteUnquoteModel(widgetId, context).markAsCurrent(widgetId, digest);
            return;
        }

        Timber.d("scraper: new");
        getQuoteUnquoteModel(widgetId, context).insertWebPage(
                widgetId,
                quotation,
                source,
                digest
        );
    }

    private void scheduleEvent(
            @NonNull Context context,
            int widgetId,
            @NonNull String notificationEvent) {
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

        getQuoteUnquoteModel(widgetId, context).markAsCurrentNext(widgetId, notificationsPreferences.getEventNextRandom());

        if (notificationsPreferences.getEventDisplayWidgetAndNotification()) {
            QuotationEntity currentQuotation
                    = getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId);

            int notificationId = getNotificationCoordinator(context).createNotificationId(currentQuotation.digest);

            getNotificationCoordinator(context).dismissNotification(context, getNotificationHelper(context), notificationId);

            displayNotification(context, widgetId, notificationEvent);
        }
    }

    private void displayNotification(
            @NonNull Context context,
            int widgetId,
            final String notificationEvent) {
        QuotationEntity currentQuotation
                = getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId);

        if (currentQuotation != null) {
            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

            int notificationId = getNotificationCoordinator(context).createNotificationId(currentQuotation.digest);

            NotificationContent notificationContent = new NotificationContent(
                    context,
                    widgetId,
                    currentQuotation.author,
                    markNotificationAsFavourite(widgetId, context, currentQuotation.digest, currentQuotation.quotation),
                    currentQuotation.digest,
                    getQuoteUnquoteModel(widgetId, context).isFavourite(currentQuotation.digest),
                    notificationsPreferences.getEventNextSequential(),
                    notificationId,
                    notificationEvent);

            displayNotificationInCorrectChannel(context, notificationContent, notificationEvent);

            getNotificationCoordinator(context).rememberNotification(notificationEvent, notificationId, currentQuotation.digest);
        }
    }

    private void displayNotificationInCorrectChannel(
            @NonNull Context context,
            @NonNull final NotificationContent notificationContent,
            final String notificationEvent) {
        Timber.d("notificationEvent=%s", notificationEvent);

        switch (notificationEvent) {
            case NotificationEvent.DEVICE_UNLOCK:
                getNotificationHelper(context).displayNotificationDeviceUnlock(notificationContent);
                break;

            case NotificationEvent.EVENT_DAILY:
                getNotificationHelper(context).displayNotificationEventDaily(notificationContent);
                break;

            case NotificationEvent.CUSTOMISABLE_INTERVAL:
                getNotificationHelper(context).displayNotificationBihourly(notificationContent);
                break;

            case NotificationEvent.TOOLBAR_PRESSED_FAVOURITE:
                switch (getNotificationCoordinator(context).getNotificationChannelId(notificationContent.getNotificationId())) {
                    case NotificationEvent.DEVICE_UNLOCK:
                        getNotificationHelper(context).displayNotificationDeviceUnlock(notificationContent);
                        break;

                    case NotificationEvent.EVENT_DAILY:
                        getNotificationHelper(context).displayNotificationEventDaily(notificationContent);
                        break;

                    case NotificationEvent.CUSTOMISABLE_INTERVAL:
                        getNotificationHelper(context).displayNotificationBihourly(notificationContent);
                        break;
                }

                break;
        }
    }

    private void onReceiveNotificationNext(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull AppWidgetManager appWidgetManager) {
        int widgetId = intent.getExtras().getInt("widgetId");
        String digest = intent.getExtras().getString("digest");
        int notificationId = intent.getExtras().getInt("notificationId");
        String notificationEvent = intent.getExtras().getString("notificationEvent");
        Timber.d("widgetId=%d; digest=%s; notificationId=%d; notificationEvent=%s",
                widgetId, digest, notificationId, notificationEvent);

        // update the widget
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);
        if (notificationsPreferences.getEventNextSequential()) {
            onReceiveToolbarPressedNextSequential(context, widgetId, appWidgetManager);
        } else {
            onReceiveToolbarPressedNextRandom(context, widgetId, appWidgetManager);
        }

        getNotificationCoordinator(context).dismissNotification(context, getNotificationHelper(context), notificationId);

        displayNotification(context, widgetId, notificationEvent);
    }

    private void onReceiveNotificationDismissed(
            @NonNull Context context,
            @NonNull final Intent intent) {
        final int notificationId = intent.getExtras().getInt("notificationId");
        getNotificationCoordinator(context).dismissNotification(context, getNotificationHelper(context), notificationId);

        getNotificationCoordinator(context).forgetNotification(notificationId);
    }

    private void onReceiveNotificationFavourite(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull AppWidgetManager appWidgetManager) {

        int widgetId = intent.getExtras().getInt("widgetId");
        String notificationDigest = intent.getStringExtra("digest");
        int notificationId = intent.getExtras().getInt("notificationId");
        String notificationEvent = intent.getExtras().getString("notificationEvent");
        Timber.d("widgetId=%d; digest=%s; notificationId=%d; notificationEvent=%s",
                widgetId, notificationDigest, notificationId, notificationEvent);

        onReceiveToolbarPressedFavourite(
                context,
                widgetId,
                notificationDigest,
                appWidgetManager);

        // update the Notification with a heart
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

        QuotationEntity quotationEntity
                = getQuoteUnquoteModel(widgetId, context).getQuotation(notificationDigest);

        NotificationContent notificationContent = new NotificationContent(
                context,
                widgetId,
                quotationEntity.author,
                markNotificationAsFavourite(widgetId, context, notificationDigest, quotationEntity.quotation),
                notificationDigest,
                getQuoteUnquoteModel(widgetId, context).isFavourite(notificationDigest),
                notificationsPreferences.getEventNextSequential(),
                notificationId,
                notificationEvent);

        displayNotificationInCorrectChannel(context, notificationContent, notificationEvent);
    }

    private String markNotificationAsFavourite(
            int widgetId, @NonNull Context context, @NonNull String digest, String quotation) {
        if (getQuoteUnquoteModel(widgetId, context).isFavourite(digest)) {
            quotation = "\u2764 " + quotation;
            return quotation;
        }

        return quotation;
    }

    private void updateNotificationFavourite(
            @NonNull Context context, int widgetId, @NonNull String widgetDigest) {

        if (getNotificationCoordinator(context).isNotificationShowingQuotation(widgetDigest)) {

            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

            QuotationEntity quotationEntity
                    = getQuoteUnquoteModel(widgetId, context).getQuotation(widgetDigest);

            NotificationContent notificationContent = new NotificationContent(
                    context,
                    widgetId,
                    quotationEntity.author,
                    markNotificationAsFavourite(widgetId, context, quotationEntity.digest, quotationEntity.quotation),
                    widgetDigest,
                    getQuoteUnquoteModel(widgetId, context).isFavourite(quotationEntity.digest),
                    notificationsPreferences.getEventNextSequential(),
                    getNotificationCoordinator(context).createNotificationId(quotationEntity.digest),
                    NotificationEvent.TOOLBAR_PRESSED_FAVOURITE);

            displayNotificationInCorrectChannel(context, notificationContent, NotificationEvent.TOOLBAR_PRESSED_FAVOURITE);
        }
    }

    private void onReceiveActivityFinishedConfiguration(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final NotificationsDailyAlarm notificationsDailyAlarm,
            @NonNull final NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm,
            @NonNull final AutoCloudBackupAlarm autoCloudBackupAlarm,
            @NonNull final ScraperAlarm scraperAlarm) {

        if (getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(widgetId) == null) {
            getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
        } else if (getQuotationsPreferences(context, widgetId).getContentSelection() != currentContentSelection) {
            getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
        } else if (getQuotationsPreferences(context, widgetId).getContentSelection().equals(ContentSelection.AUTHOR)
                && !getQuotationsPreferences(context, widgetId).getContentSelectionAuthor().equals(currentAuthorSelection)) {
            getQuoteUnquoteModel(widgetId, context).markAsCurrentDefault(widgetId);
        }

        manageAlarms(context, widgetId,
                notificationsDailyAlarm, notificationsCustomisableIntervalAlarm, autoCloudBackupAlarm, scraperAlarm);
    }

    private void manageAlarms(
            @NonNull Context context, int widgetId,
            @NonNull NotificationsDailyAlarm notificationsDailyAlarm,
            @NonNull NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm,
            @NonNull AutoCloudBackupAlarm autoCloudBackupAlarm,
            @NonNull ScraperAlarm scraperAlarm) {

        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);
        if (quotationsPreferences.getDatabaseExternalWeb()) {
            scraperAlarm.setAlarm();
        } else {
            scraperAlarm.resetAlarm();
        }

        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);
        if (notificationsPreferences.getEventDaily()) {
            notificationsDailyAlarm.setAlarm();
        } else {
            notificationsDailyAlarm.resetAlarm();
        }

        if (notificationsPreferences.getCustomisableInterval()) {
            notificationsCustomisableIntervalAlarm.setAlarm();
        } else {
            notificationsCustomisableIntervalAlarm.resetAlarm();
        }

        SyncPreferences syncPreferences = new SyncPreferences(widgetId, context);
        Timber.d("autoCloudBackupAlarm.manageAlarms");
        if (!syncPreferences.getAutoCloudBackup()) {
            autoCloudBackupAlarm.resetAlarm();
        }
    }

    private void setTransparency(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        AppearancePreferences appearancePreferences = this.getAppearancePreferences(context, widgetId);

        if (appearancePreferences.getAppearanceForceFollowSystemTheme() == false) {
            int seekBarValue = appearancePreferences.getAppearanceTransparency();
            seekBarValue = seekBarValue / 10;

            String appearanceColour = appearancePreferences.getAppearanceColour();

            int transparencyMask = this.getTransparencyMask(seekBarValue, appearanceColour);

            if (new AppearancePreferences(context).getAppearanceForceFollowSystemTheme() == true) {
                transparencyMask = this.getTransparencyMask(
                        -1,
                        AppearancePreferences.DEFAULT_COLOUR_BACKGROUND);
            }

            final String setBackgroundColor = "setBackgroundColor";
            remoteViews.setInt(R.id.linearLayoutQuotation, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonFirst, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonPrevious, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonFavourite, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonShare, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonJump, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonNextRandom, setBackgroundColor, transparencyMask);
            remoteViews.setInt(R.id.imageButtonNextSequential, setBackgroundColor, transparencyMask);
        }
    }

    public int getTransparencyMask(
            final int seekBarValue,
            @NonNull final String appearanceColour) {

        float transparency = 1;
        if (seekBarValue != -1) {
            transparency -= seekBarValue * .1f;
        }

        final String hex = appearanceColour.replace("#FF", "");
        return (int) (transparency * 0xFF) << 24 | (int) Long.parseLong(hex, 16);
    }

    private void setToolbarButtons(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews,
            @NonNull String appearanceToolbarColour) {
        final AppearancePreferences appearancePreferences = getAppearancePreferences(context, widgetId);

        if (!appearancePreferences.getAppearanceToolbarFirst()
                && !appearancePreferences.getAppearanceToolbarPrevious()
                && !appearancePreferences.getAppearanceToolbarFavourite()
                && !appearancePreferences.getAppearanceToolbarShare()
                && !appearancePreferences.getAppearanceToolbarJump()
                && !appearancePreferences.getAppearanceToolbarRandom()
                && !appearancePreferences.getAppearanceToolbarSequential()) {
            setToolbarVisibility(remoteViews, false);
        } else {
            setToolbarVisibility(remoteViews, true);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarFirst(),
                    appearanceToolbarColour,
                    R.id.imageButtonFirst);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarPrevious(),
                    appearanceToolbarColour,
                    R.id.imageButtonPrevious);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarFavourite(),
                    appearanceToolbarColour,
                    R.id.imageButtonFavourite);

            setHeartColour(context, widgetId, remoteViews);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarShare(),
                    appearanceToolbarColour,
                    R.id.imageButtonShare);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarJump(),
                    appearanceToolbarColour,
                    R.id.imageButtonJump);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarRandom(),
                    appearanceToolbarColour,
                    R.id.imageButtonNextRandom);

            setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarSequential(),
                    appearanceToolbarColour,
                    R.id.imageButtonNextSequential);
        }
    }

    private void setToolbarVisibility(
            @NonNull final RemoteViews remoteViews,
            final boolean toolbarEnabled) {
        if (toolbarEnabled) {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.GONE);
        }
    }

    @NonNull
    public AppearancePreferences getAppearancePreferences(@NonNull final Context context, final int widgetId) {
        return new AppearancePreferences(widgetId, context);
    }

    private void setToolbarButtonVisibility(
            @NonNull final RemoteViews remoteViews,
            final boolean toolbarButtonEnabled,
            final String colour,
            @IdRes final int imageButtonId) {

        if (toolbarButtonEnabled) {
            remoteViews.setViewVisibility(imageButtonId, View.VISIBLE);

            remoteViews.setInt(
                    imageButtonId,
                    "setColorFilter",
                    Color.parseColor(colour));

        } else {
            remoteViews.setViewVisibility(imageButtonId, View.GONE);
        }
    }

    public void setHeartColour(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        final QuotationEntity quotationEntity = getQuoteUnquoteModel(widgetId, context).getCurrentQuotation(
                widgetId);

        String appearanceToolbarColour = getAppearancePreferences(context, widgetId).getAppearanceToolbarColour();

        boolean isFavourite = quotationEntity != null && getQuoteUnquoteModel(widgetId, context).isFavourite(quotationEntity.digest);

        if (isFavourite) {
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_red_24);
        }

        AppearancePreferences appearancePreferences = new AppearancePreferences(context);
        if (appearancePreferences.getAppearanceForceFollowSystemTheme()) {
            if (isNightMode(context)) {
                if (!isFavourite) {
                    remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_ffffffff_24);
                }
            } else {
                if (!isFavourite) {
                    remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_ff000000_24);
                }
            }
        } else {
            if (!isFavourite) {
                remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_ff000000_24);
            }

            // black toolbar button colour always has a red heart
            if (appearanceToolbarColour.equals("#FF000000") && isFavourite) {
                remoteViews.setInt(
                        R.id.imageButtonFavourite,
                        "setColorFilter",
                        Color.RED
                );
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
            Timber.d("widgetId=%d", widgetId);

            NotificationManagerCompat.from(context).cancel(widgetId);

            getQuoteUnquoteModel(widgetId, context).delete(widgetId);
            PreferencesFacade.delete(context, widgetId);

            final NotificationsDailyAlarm notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);
            notificationsDailyAlarm.resetAlarm();

            final NotificationsCustomisableIntervalAlarm notificationsCustomisableIntervalAlarm = new NotificationsCustomisableIntervalAlarm(context, widgetId);
            notificationsCustomisableIntervalAlarm.resetAlarm();

            final ScraperAlarm scraperAlarm = new ScraperAlarm(context, widgetId);
            scraperAlarm.resetAlarm();
        }
    }

    @Override
    public void onDisabled(@NonNull final Context context) {
        // last widget instance deleted
        super.onDisabled(context);

        try {
            getQuoteUnquoteModel(-1, context).disable();
            final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(context);
            final String localCode = quotationsPreferences.getContentLocalCode();
            PreferencesFacade.erase(context);
            Timber.d("setting LocalCode");
            quotationsPreferences.setContentLocalCode(localCode);

            if (CloudService.isRunning) {
                context.stopService(new Intent(context, CloudServiceBackup.class));
            }

            if (CloudService.isRunning) {
                context.stopService(new Intent(context, CloudServiceRestore.class));
            }
        } finally {
            stopDatabaseConnectivity();
            stopExecutorService();
        }
    }

    @Nullable
    public synchronized QuoteUnquoteModel getQuoteUnquoteModel(
            int widgetId, @NonNull final Context context) {
        if (quoteUnquoteModel == null) {
            quoteUnquoteModel = new QuoteUnquoteModel(widgetId, context);
        } else {
            if (getQuotationsPreferences(context, widgetId).getDatabaseInternal()) {
                quoteUnquoteModel.databaseRepository.useInternalDatabase = true;
            } else {
                quoteUnquoteModel.databaseRepository.useInternalDatabase = false;
            }
        }

        return quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }
}
