package com.github.jameshnsears.quoteunquote;

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
import androidx.core.app.NotificationManagerCompat;

import com.github.jameshnsears.quoteunquote.cloud.CloudService;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceBackup;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceRestore;
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsDailyAlarm;
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationHelper;
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class QuoteUnquoteWidget extends AppWidgetProvider {
    @Nullable
    private static ExecutorService executorService;

    private static volatile boolean receiversRegistered;

    @NonNull
    private final NotificationHelper notificationHelper = new NotificationHelper();

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

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

    @Nullable
    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(5);
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

    @Override
    public void onEnabled(@NonNull final Context context) {
        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(context);
        if (quotationsPreferences.getContentLocalCode().equals("")) {
            Timber.d("setting LocalCode");
            quotationsPreferences.setContentLocalCode(CloudTransferHelper.getLocalCode());
        }

        startDatabaseConnectivity(context);
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
                    R.id.imageButtonNextRandom,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM));

            remoteViews.setOnClickPendingIntent(
                    R.id.imageButtonNextSequential,
                    IntentFactoryHelper.createClickPendingIntent(context, widgetId, IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL));

            if (widgetId != 0) {
                setTransparency(context, widgetId, remoteViews);

                setToolbarButtonColours(context, widgetId, remoteViews);

                setToolbarButtonsVisibility(context, widgetId, remoteViews);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        // at end, so that onReceive gets called first
        super.onUpdate(context, appWidgetManager, widgetIds);
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);

        final int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Timber.d("%d: action=%s", widgetId, intent.getAction());

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        final NotificationsDailyAlarm notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);

        try {
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    startDatabaseConnectivity(context);
                    onReceiveDeviceUnlock(context, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_FAVOURITE:
                    onReceiveNotificationFavourite(context, widgetId, intent, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_NEXT:
                    onReceiveNotificationNext(context, widgetId, intent, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NOTIFICATION_DELETED:
                    onReceiveNotificationDismissed(context, widgetId, intent, appWidgetManager);
                    break;

                /*
                adb shell
                am broadcast -a android.intent.action.BOOT_COMPLETED
                */
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_REBOOT:
                case IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION:
                    onReceiveActivityFinishedConfiguration(context, widgetId, notificationsDailyAlarm);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    onReceiveDailyAlarm(context, widgetId, notificationsDailyAlarm);
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
                            getQuoteUnquoteModel(context).getCurrentQuotation(widgetId).digest,
                            appWidgetManager);
                    break;

                case IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION:
                    onReceiveAllWidgetInstancesFavouriteNotification(context, widgetId, appWidgetManager);
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

                case AppWidgetManager.ACTION_APPWIDGET_ENABLED:
                    onReceiveActionAppwidgetEnabled(context, appWidgetManager);
                    break;

                default:
                    break;
            }
        } finally {
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveActionAppwidgetEnabled(@NonNull final Context context, final AppWidgetManager appWidgetManager) {
        NotificationsDailyAlarm notificationsDailyAlarm;
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));
        for (final int widgetId : widgetIds) {
            Timber.d("setDailyAlarm: %d", widgetId);
            notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);
            notificationsDailyAlarm.setDailyAlarm();
        }
    }

    private void onReceiveAllWidgetInstancesFavouriteNotification(
            @NonNull final Context context,
            final int widgetId,
            final AppWidgetManager appWidgetManager) {
        Timber.d("%d", widgetId);
        setHeartColour(
                context,
                widgetId,
                new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget));

        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

        int favouritesCount = getQuoteUnquoteModel(context).countFavouritesWithoutRx();

        if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
            if (favouritesCount == 0) {
                noFavouritesSoMoveToAll(context, widgetId, quotationsPreferences);
            } else {
                getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void startDatabaseConnectivity(@NonNull Context context) {
        setQuoteUnquoteModel(new QuoteUnquoteModel(context));
    }

    public void stopDatabaseConnectivity() {
        quoteUnquoteModel = null;
    }

    private void onReceiveDeviceUnlock(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager) {
        Timber.d("onReceiveDeviceUnlock");

        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));

        for (final int widgetId : widgetIds) {
            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);
            Timber.d("%d: getEventDeviceUnlock=%b", widgetId, notificationsPreferences.getEventDeviceUnlock());
            if (notificationsPreferences.getEventDeviceUnlock()) {
                scheduleEvent(context, widgetId);
            }
        }

        onUpdate(context, appWidgetManager, widgetIds);
    }

    public void onReceiveToolbarPressedShare(@NonNull final Context context, final int widgetId) {
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                getQuoteUnquoteModel(context).getCurrentQuotation(widgetId).theShareContent()));
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull final Context context,
            final int widgetId,
            @NonNull String digest,
            @NonNull final AppWidgetManager appWidgetManager) {
        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

        int favouritesCount = getQuoteUnquoteModel(context).toggleFavourite(widgetId, digest);

        if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
            if (favouritesCount == 0) {
                noFavouritesSoMoveToAll(context, widgetId, quotationsPreferences);
            } else {
                getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }

        sendAllWidgetInstancesFavouriteNotification(context, widgetId, appWidgetManager);

        updateNotificationIfShowingFavouriteDigest(
                context,
                widgetId,
                getQuoteUnquoteModel(context).getCurrentQuotation(widgetId).digest);
    }

    private void sendAllWidgetInstancesFavouriteNotification(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        for (int id : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            if (id != widgetId) {
                Timber.d("allInstancesFavouriteNotification: from=%d; send=%d", widgetId, id);
                final Intent instancesIntent = IntentFactoryHelper.createIntent(context, id);
                instancesIntent.setAction(IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION);
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
            getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        }
    }

    public void onReceiveToolbarPressedFirst(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(context).resetPrevious(widgetId, new QuotationsPreferences(widgetId, context).getContentSelection());
        getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);

        updateNotificationIfExists(context, widgetId);
    }

    private void onReceiveToolbarPressedPrevious(
            @NonNull final Context context, final int widgetId, @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModel(context).markAsCurrentPrevious(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);

        updateNotificationIfExists(context, widgetId);
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

        updateNotificationIfExists(context, widgetId);
    }

    private void onReceiveDailyAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final NotificationsDailyAlarm notificationsDailyAlarm) {
        notificationsDailyAlarm.setDailyAlarm();
        scheduleEvent(context, widgetId);
    }

    private void scheduleEvent(@NonNull Context context, int widgetId) {
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

        getQuoteUnquoteModel(context).markAsCurrentNext(widgetId, notificationsPreferences.getEventNextRandom());

        if (notificationsPreferences.getEventDisplayWidgetAndNotification()) {
            displayNotification(context, widgetId);
        }
    }

    @NonNull
    static Map<Integer, String> widgetIdToDigestNotificationMap = new HashMap();

    private void displayNotification(@NonNull Context context, int widgetId) {
        QuotationEntity currentQuotation
                = getQuoteUnquoteModel(context).getCurrentQuotation(widgetId);

        if (currentQuotation != null){
            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

            notificationHelper.displayNotification(
                    context,
                    widgetId,
                    currentQuotation.author,
                    markNotificationAsFavourite(
                            context, currentQuotation.digest, currentQuotation.quotation),
                    currentQuotation.digest,
                    getQuoteUnquoteModel(context).isFavourite(currentQuotation.digest),
                    notificationsPreferences.getEventNextSequential(),
                    widgetId);

            // each widget only ever displays one notification, the notificationId is the widgetId
            // the widgetId is mapped to a digest so we can handle Favourite appropriately
            widgetIdToDigestNotificationMap.put(widgetId, currentQuotation.digest);
        }
    }

    private void onReceiveNotificationDismissed(
            @NonNull Context context, int widgetId, @NonNull Intent intent,
            @NonNull AppWidgetManager appWidgetManager) {

        widgetIdToDigestNotificationMap.remove(widgetId);
    }

    private void onReceiveNotificationNext(
            @NonNull Context context, int widgetId, @NonNull Intent intent,
            @NonNull AppWidgetManager appWidgetManager) {
        // update the widget
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);
        if (notificationsPreferences.getEventNextSequential()) {
            onReceiveToolbarPressedNextSequential(context, widgetId, appWidgetManager);
        } else {
            onReceiveToolbarPressedNextRandom(context, widgetId, appWidgetManager);
        }

        displayNotification(context, widgetId);
    }

    private void onReceiveNotificationFavourite(
            @NonNull Context context, int widgetId, @NonNull Intent intent,
            @NonNull AppWidgetManager appWidgetManager) {

        String digestFromIntent = intent.getStringExtra("digest");

        // toggle Favourite in widgets, whether digest visible or not in widgets
        onReceiveToolbarPressedFavourite(
                context,
                widgetId,
                digestFromIntent,
                appWidgetManager);

        sendAllWidgetInstancesFavouriteNotification(context, widgetId, appWidgetManager);

        // update the Notification
        NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

        QuotationEntity quotationEntity
                = getQuoteUnquoteModel(context).getQuotation(digestFromIntent);

        notificationHelper.displayNotification(
                context,
                widgetId,
                quotationEntity.author,
                markNotificationAsFavourite(context, digestFromIntent, quotationEntity.quotation),
                digestFromIntent,
                getQuoteUnquoteModel(context).isFavourite(digestFromIntent),
                notificationsPreferences.getEventNextSequential(),
                widgetId);
    }

    private String markNotificationAsFavourite(
            @NonNull Context context, @NonNull String digest, String quotation) {
         if (getQuoteUnquoteModel(context).isFavourite(digest)) {
            quotation = "\u2764 " + quotation;
            return quotation;
        }

        return quotation;
    }

    private void updateNotificationIfExists(@NonNull Context context, int widgetId) {
        if (!widgetIdToDigestNotificationMap.isEmpty()) {
            displayNotification(context, widgetId);
        }
    }

    private void updateNotificationIfShowingFavouriteDigest(
            @NonNull Context context, int widgetId, @NonNull String digestMadeFavouriteInWidget) {

        // only update a notification if there is a notification to update!
        if (!widgetIdToDigestNotificationMap.isEmpty()
                && widgetIdToDigestNotificationMap.get(widgetId).equals(digestMadeFavouriteInWidget)) {

            NotificationsPreferences notificationsPreferences = new NotificationsPreferences(widgetId, context);

            QuotationEntity quotationEntity
                    = getQuoteUnquoteModel(context).getQuotation(digestMadeFavouriteInWidget);

            notificationHelper.displayNotification(
                    context,
                    widgetId,
                    quotationEntity.author,
                    markNotificationAsFavourite(context, digestMadeFavouriteInWidget, quotationEntity.quotation),
                    digestMadeFavouriteInWidget,
                    getQuoteUnquoteModel(context).isFavourite(digestMadeFavouriteInWidget),
                    notificationsPreferences.getEventNextSequential(),
                    widgetId);
        }
}

    private void onReceiveActivityFinishedConfiguration(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final NotificationsDailyAlarm notificationsDailyAlarm) {
        Timber.d("%d", widgetId);
        getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        notificationsDailyAlarm.setDailyAlarm();
        updateNotificationIfExists(context, widgetId);
    }

    private void setTransparency(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        int seekBarValue = this.getAppearancePreferences(context, widgetId).getAppearanceTransparency();
        seekBarValue = seekBarValue / 10;

        final String appearanceColour = this.getAppearancePreferences(context, widgetId).getAppearanceColour();

        final int transparencyMask = this.getTransparencyMask(seekBarValue, appearanceColour);

        final String setBackgroundColor = "setBackgroundColor";
        remoteViews.setInt(R.id.listViewQuotation, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonFirst, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonPrevious, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonFavourite, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonShare, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonNextRandom, setBackgroundColor, transparencyMask);
        remoteViews.setInt(R.id.imageButtonNextSequential, setBackgroundColor, transparencyMask);
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

    private void setToolbarButtonsVisibility(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        final AppearancePreferences appearancePreferences = this.getAppearancePreferences(context, widgetId);

        if (!appearancePreferences.getAppearanceToolbarFirst()
                && !appearancePreferences.getAppearanceToolbarPrevious()
                && !appearancePreferences.getAppearanceToolbarFavourite()
                && !appearancePreferences.getAppearanceToolbarShare()
                && !appearancePreferences.getAppearanceToolbarRandom()
                && !appearancePreferences.getAppearanceToolbarSequential()) {
            setToolbarVisibility(remoteViews, false);
        } else {
            setToolbarVisibility(remoteViews, true);

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

    private void setToolbarButtonColours(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        if (this.getAppearancePreferences(context, widgetId).getAppearanceToolbarColour().equals("#FFFFFFFF")) {
            remoteViews.setImageViewResource(R.id.imageButtonFirst, R.drawable.ic_toolbar_first_ffffffff_24);
            remoteViews.setImageViewResource(R.id.imageButtonPrevious, R.drawable.ic_toolbar_previous_ffffffff_24);
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_ffffffff_24);
            remoteViews.setImageViewResource(R.id.imageButtonShare, R.drawable.ic_toolbar_share_ffffffff_24);
            remoteViews.setImageViewResource(R.id.imageButtonNextSequential, R.drawable.ic_toolbar_next_sequential_ffffffff_24);
            remoteViews.setImageViewResource(R.id.imageButtonNextRandom, R.drawable.ic_toolbar_next_random_ffffffff_24);
        } else {
            // "#FF000000":
            remoteViews.setImageViewResource(R.id.imageButtonFirst, R.drawable.ic_toolbar_first_ff000000_24);
            remoteViews.setImageViewResource(R.id.imageButtonPrevious, R.drawable.ic_toolbar_previous_ff000000_24);
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_ff000000_24);
            remoteViews.setImageViewResource(R.id.imageButtonShare, R.drawable.ic_toolbar_share_ff000000_24);
            remoteViews.setImageViewResource(R.id.imageButtonNextSequential, R.drawable.ic_toolbar_next_sequential_ff000000_24);
            remoteViews.setImageViewResource(R.id.imageButtonNextRandom, R.drawable.ic_toolbar_next_random_ff000000_24);
        }

        setHeartColour(context, widgetId, remoteViews);
    }

    @NonNull
    public AppearancePreferences getAppearancePreferences(@NonNull final Context context, final int widgetId) {
        return new AppearancePreferences(widgetId, context);
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

        if (quotationEntity != null && getQuoteUnquoteModel(context).isFavourite(quotationEntity.digest)) {
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_red_24);
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

            NotificationManagerCompat.from(context).cancel(widgetId);

            getQuoteUnquoteModel(context).delete(widgetId);
            PreferencesFacade.delete(context, widgetId);

            final NotificationsDailyAlarm notificationsDailyAlarm = new NotificationsDailyAlarm(context, widgetId);
            notificationsDailyAlarm.resetAnyExistingDailyAlarm();
        }
    }

    @Override
    public void onDisabled(@NonNull final Context context) {
        // last widget instance deleted
        super.onDisabled(context);

        try {
            getQuoteUnquoteModel(context).disable();
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
