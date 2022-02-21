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

import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceSend;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventDailyAlarm;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.listview.ListViewService;
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
    private static volatile boolean receiversRegistered;
    @NonNull
    private final NotificationHelper notificationHelper = new NotificationHelper();
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    private static void registerReceivers(@NonNull final Context contextIn) {
        Timber.d("receiversRegistered=%b", QuoteUnquoteWidget.receiversRegistered);

        if (!QuoteUnquoteWidget.receiversRegistered) {
            final Context context = contextIn.getApplicationContext();
            final QuoteUnquoteWidget receiver = new QuoteUnquoteWidget();

            final IntentFilter userPresent = new IntentFilter();
            userPresent.addAction("android.intent.action.USER_PRESENT");
            context.registerReceiver(receiver, userPresent);

            final IntentFilter bootCompleted = new IntentFilter();
            bootCompleted.addAction("android.intent.action.BOOT_COMPLETED");
            context.registerReceiver(receiver, bootCompleted);

            final IntentFilter quickBootPowerOn = new IntentFilter();
            quickBootPowerOn.addAction("android.intent.action.QUICKBOOT_POWERON");
            context.registerReceiver(receiver, quickBootPowerOn);

            QuoteUnquoteWidget.receiversRegistered = true;
        }
    }

    @Nullable
    public static ExecutorService getExecutorService() {
        if (QuoteUnquoteWidget.executorService == null) {
            QuoteUnquoteWidget.executorService = Executors.newFixedThreadPool(5);
        }
        Timber.d(QuoteUnquoteWidget.executorService.toString());
        return QuoteUnquoteWidget.executorService;
    }

    public static void stopExecutorService() {
        if (QuoteUnquoteWidget.executorService != null) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                QuoteUnquoteWidget.executorService.shutdown();
                try {
                    if (!QuoteUnquoteWidget.executorService.awaitTermination(5000, TimeUnit.MICROSECONDS)) {
                        Timber.d("awaitTermination=timeout");
                    }
                } catch (@NonNull final InterruptedException e) {
                    Timber.e(e);
                    Thread.currentThread().interrupt();
                }
                Timber.d(QuoteUnquoteWidget.executorService.toString());
            }));
        }
    }

    @Override
    public void onEnabled(@NonNull Context context) {
        ContentPreferences contentPreferences = new ContentPreferences(context);
        if (contentPreferences.getContentFavouritesLocalCode().equals("")) {
            Timber.d("setting LocalCode");
            contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());
        }
        this.startDatabaseConnectivity(context);
    }

    @Override
    public void onUpdate(
            @NonNull Context context,
            @NonNull AppWidgetManager appWidgetManager,
            @NonNull int[] widgetIds) {
        QuoteUnquoteWidget.registerReceivers(context);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget);

        for (int widgetId : widgetIds) {
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
                this.setTransparency(context, widgetId, remoteViews);

                this.setToolbarButtonColours(context, widgetId, remoteViews);

                this.setToolbarButtonsVisibility(context, widgetId, remoteViews);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        // at end, so that onReceive gets called first
        super.onUpdate(context, appWidgetManager, widgetIds);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        int widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Timber.d("%d: action=%s", widgetId, intent.getAction());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        EventDailyAlarm eventDailyAlarm = new EventDailyAlarm(context, widgetId);

        try {
            switch (intent.getAction()) {
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    this.onReceiveMyPackageReplaced(context, widgetId, appWidgetManager);
                    break;

                case Intent.ACTION_USER_PRESENT:
                    this.onReceiveDeviceUnlock(context, appWidgetManager);
                    break;

                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_REBOOT:
                /*
                adb shell
                am broadcast -a android.intent.action.BOOT_COMPLETED
                 */
                case IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION:
                    this.onReceiveActivityFinishedConfiguration(context, widgetId, eventDailyAlarm);
                    break;

                case IntentFactoryHelper.ACTIVITY_FINISHED_REPORT:
                    this.onReceiveActivityFinishedReport(widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.DAILY_ALARM:
                    this.onReceiveDailyAlarm(context, widgetId, eventDailyAlarm);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FIRST:
                    this.onReceiveToolbarPressedFirst(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS:
                    this.onReceiveToolbarPressedPrevious(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE:
                    this.onReceiveToolbarPressedFavourite(context, widgetId, appWidgetManager);
                    this.sendAllInstancesFavouriteNotification(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION:
                    onReceiveAllWidgetInstancesFavouriteNotification(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_SHARE:
                    this.onReceiveToolbarPressedShare(context, widgetId);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM:
                    this.onReceiveToolbarPressedNextRandom(context, widgetId, appWidgetManager);
                    break;

                case IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL:
                    this.onReceiveToolbarPressedNextSequential(context, widgetId, appWidgetManager);
                    break;

                case AppWidgetManager.ACTION_APPWIDGET_ENABLED:
                    onReceiveActionAppwidgetEnabled(context, appWidgetManager);
                    break;

                default:
                    break;
            }
        } finally {
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
                this.onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveActionAppwidgetEnabled(@NonNull Context context, AppWidgetManager appWidgetManager) {
        EventDailyAlarm eventDailyAlarm;
        final int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));
        for (int widgetId : widgetIds) {
            Timber.d("setDailyAlarm: %d", widgetId);
            eventDailyAlarm = new EventDailyAlarm(context, widgetId);
            eventDailyAlarm.setDailyAlarm();
        }
    }

    private void onReceiveAllWidgetInstancesFavouriteNotification(
            @NonNull Context context,
            int widgetId,
            AppWidgetManager appWidgetManager) {
        Timber.d("allInstancesFavouriteNotification: receive=%d", widgetId);
        this.setHeartColour(
                context,
                widgetId,
                new RemoteViews(context.getPackageName(), R.layout.quote_unquote_widget));

        ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        final int favouritesCount = this.getQuoteUnquoteModel(context).countFavouritesWithoutRx();

        if (contentPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
            if (favouritesCount == 0) {
                this.noFavouritesSoMoveToAll(context, widgetId, contentPreferences);
            } else {
                this.getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void onReceiveMyPackageReplaced(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        Timber.d("performing update: %d", widgetId);

        this.stopDatabaseConnectivity();
        QuoteUnquoteWidget.stopExecutorService();

        DatabaseRepository.resetDatabaseInstances(context);

        this.startDatabaseConnectivity(context);

        for (final int id : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            Timber.d("%d", id);
            if (id != 0) {
                PackageReplacedHelper packageReplacedHelper = new PackageReplacedHelper(id, context);
                packageReplacedHelper.alignHistoryWithQuotations(this.getQuoteUnquoteModel(context));
                packageReplacedHelper.migratePreferences();
            }
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.listViewQuotation);
        }
    }

    private void startDatabaseConnectivity(@NonNull final Context context) {
        this.setQuoteUnquoteModel(new QuoteUnquoteModel(context));
    }

    public void stopDatabaseConnectivity() {
        this.quoteUnquoteModel = null;
    }

    private void onReceiveActivityFinishedReport(
            int widgetId, @NonNull AppWidgetManager appWidgetManager) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDeviceUnlock(
            @NonNull Context context,
            @NonNull AppWidgetManager appWidgetManager) {
        final int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class));

        for (int widgetId : widgetIds) {
            if (new EventPreferences(widgetId, context).getEventDeviceUnlock()) {
                this.scheduleEvent(context, widgetId);
            }
        }

        this.onUpdate(context, appWidgetManager, widgetIds);
    }

    public void onReceiveToolbarPressedShare(@NonNull Context context, int widgetId) {
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                this.getQuoteUnquoteModel(context).getCurrentQuotation(widgetId).theQuotation()));
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        final QuotationEntity currentQuotation = this.getQuoteUnquoteModel(context).getCurrentQuotation(
                widgetId);

        final int favouritesCount = this.getQuoteUnquoteModel(context).toggleFavourite(
                widgetId, currentQuotation.digest);

        if (contentPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
            if (favouritesCount == 0) {
                this.noFavouritesSoMoveToAll(context, widgetId, contentPreferences);
            } else {
                this.getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
            }

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        }
    }

    private void sendAllInstancesFavouriteNotification(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        for (final int id : appWidgetManager.getAppWidgetIds(new ComponentName(context, QuoteUnquoteWidget.class))) {
            if (id != widgetId) {
                Timber.d("allInstancesFavouriteNotification: from=%d; send=%d", widgetId, id);
                Intent instancesIntent = IntentFactoryHelper.createIntent(context, id);
                instancesIntent.setAction(IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION);
                context.sendBroadcast(instancesIntent);
            }
        }
    }

    private void noFavouritesSoMoveToAll(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final ContentPreferences contentPreferences) {
        Timber.d("%s", contentPreferences.getContentSelection());

        if (contentPreferences.getContentSelection() != ContentSelection.ALL) {
            contentPreferences.setContentSelection(ContentSelection.ALL);
            Timber.d("%s", contentPreferences.getContentSelection());
            this.getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        }
    }

    public void onReceiveToolbarPressedFirst(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        this.getQuoteUnquoteModel(context).resetPrevious(widgetId, new ContentPreferences(widgetId, context).getContentSelection());
        this.getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedPrevious(
            @NonNull Context context, int widgetId, @NonNull AppWidgetManager appWidgetManager) {
        this.getQuoteUnquoteModel(context).markAsCurrentPrevious(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedNextRandom(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        this.onReceiveToolbarPressedNext(context, widgetId, appWidgetManager, true);
    }

    private void onReceiveToolbarPressedNextSequential(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager) {
        this.onReceiveToolbarPressedNext(context, widgetId, appWidgetManager, false);
    }

    private void onReceiveToolbarPressedNext(
            @NonNull Context context,
            int widgetId,
            @NonNull AppWidgetManager appWidgetManager,
            boolean randomNext) {
        this.getQuoteUnquoteModel(context).markAsCurrentNext(widgetId, randomNext);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveDailyAlarm(
            @NonNull Context context,
            int widgetId,
            @NonNull EventDailyAlarm scheduleDailyAlarm) {
        scheduleDailyAlarm.setDailyAlarm();
        this.scheduleEvent(context, widgetId);
    }

    private void scheduleEvent(@NonNull final Context context, final int widgetId) {
        final EventPreferences eventPreferences = new EventPreferences(widgetId, context);

        this.getQuoteUnquoteModel(context).markAsCurrentNext(widgetId, eventPreferences.getEventNextRandom());

        if (eventPreferences.getEventDisplayWidgetAndNotification()) {
            final QuotationEntity currentQuotation = this.getQuoteUnquoteModel(context).getCurrentQuotation(
                    widgetId);

            this.notificationHelper.displayNotification(context, currentQuotation);
        }
    }

    private void onReceiveActivityFinishedConfiguration(
            @NonNull Context context,
            int widgetId,
            @NonNull EventDailyAlarm eventDailyAlarm) {
        Timber.d("%d", widgetId);
        this.getQuoteUnquoteModel(context).markAsCurrentDefault(widgetId);
        eventDailyAlarm.setDailyAlarm();
    }

    private void setTransparency(
            @NonNull Context context,
            int widgetId,
            @NonNull RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        int seekBarValue = getAppearancePreferences(context, widgetId).getAppearanceTransparency();
        seekBarValue = seekBarValue / 10;

        String appearanceColour = getAppearancePreferences(context, widgetId).getAppearanceColour();

        int transparencyMask = getTransparencyMask(seekBarValue, appearanceColour);

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
            int seekBarValue,
            @NonNull String appearanceColour) {

        float transparency = 1;
        if (seekBarValue != -1) {
            transparency -= seekBarValue * .1f;
        }

        String hex = appearanceColour.replace("#FF", "");
        return (int) (transparency * 0xFF) << 24 | (int) Long.parseLong(hex, 16);
    }

    private void setToolbarButtonsVisibility(
            @NonNull Context context,
            int widgetId,
            @NonNull RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        AppearancePreferences appearancePreferences = getAppearancePreferences(context, widgetId);

        if (!appearancePreferences.getAppearanceToolbarFirst()
                && !appearancePreferences.getAppearanceToolbarPrevious()
                && !appearancePreferences.getAppearanceToolbarFavourite()
                && !appearancePreferences.getAppearanceToolbarShare()
                && !appearancePreferences.getAppearanceToolbarRandom()
                && !appearancePreferences.getAppearanceToolbarSequential()) {
            this.setToolbarVisibility(remoteViews, false);
        } else {
            this.setToolbarVisibility(remoteViews, true);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarFirst(),
                    R.id.imageButtonFirst);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarPrevious(),
                    R.id.imageButtonPrevious);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarFavourite(),
                    R.id.imageButtonFavourite);

            this.setHeartColour(context, widgetId, remoteViews);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarShare(),
                    R.id.imageButtonShare);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarRandom(),
                    R.id.imageButtonNextRandom);

            this.setToolbarButtonVisibility(
                    remoteViews,
                    appearancePreferences.getAppearanceToolbarSequential(),
                    R.id.imageButtonNextSequential);
        }
    }

    private void setToolbarVisibility(
            @NonNull RemoteViews remoteViews,
            boolean toolbarEnabled) {
        if (toolbarEnabled) {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.linearLayoutToolbar, View.GONE);
        }
    }

    private void setToolbarButtonColours(
            @NonNull Context context,
            int widgetId,
            @NonNull RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        if (getAppearancePreferences(context, widgetId).getAppearanceToolbarColour().equals("#FFFFFFFF")) {
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

        this.setHeartColour(context, widgetId, remoteViews);
    }

    @NonNull
    public AppearancePreferences getAppearancePreferences(@NonNull Context context, int widgetId) {
        return new AppearancePreferences(widgetId, context);
    }

    private void setToolbarButtonVisibility(
            @NonNull RemoteViews remoteViews,
            boolean toolbarButtonEnabled,
            @IdRes int imageButtonId) {

        if (toolbarButtonEnabled) {
            remoteViews.setViewVisibility(imageButtonId, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(imageButtonId, View.GONE);
        }
    }

    public void setHeartColour(
            @NonNull Context context,
            int widgetId,
            @NonNull RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        QuotationEntity quotationEntity = this.getQuoteUnquoteModel(context).getCurrentQuotation(
                widgetId);

        if (quotationEntity != null && this.getQuoteUnquoteModel(context).isFavourite(quotationEntity.digest)) {
            remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_red_24);
        }
    }

    @Override
    public void onDeleted(
            @NonNull Context context,
            @NonNull int[] widgetIds) {
        // a widget instance deleted
        super.onDeleted(context, widgetIds);

        for (int widgetId : widgetIds) {
            Timber.d("%d", widgetId);

            this.getQuoteUnquoteModel(context).delete(widgetId);
            PreferencesFacade.delete(context, widgetId);

            EventDailyAlarm eventDailyAlarm = new EventDailyAlarm(context, widgetId);
            eventDailyAlarm.resetAnyExistingDailyAlarm();
        }
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        // last widget instance deleted
        super.onDisabled(context);

        try {
            this.getQuoteUnquoteModel(context).disable();
            ContentPreferences contentPreferences = new ContentPreferences(context);
            String localCode = contentPreferences.getContentFavouritesLocalCode();
            PreferencesFacade.disable(context);
            Timber.d("setting LocalCode");
            contentPreferences.setContentFavouritesLocalCode(localCode);

            if (CloudServiceSend.isRunning) {
                context.stopService(new Intent(context, CloudServiceSend.class));
            }
        } finally {
            this.stopDatabaseConnectivity();
            QuoteUnquoteWidget.stopExecutorService();
        }
    }

    @Nullable
    public QuoteUnquoteModel getQuoteUnquoteModel(@NonNull Context context) {
        if (this.quoteUnquoteModel == null) {
            this.quoteUnquoteModel = new QuoteUnquoteModel(context);
        }
        return this.quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable final QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }
}
