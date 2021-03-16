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
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import timber.log.Timber;

public final class QuoteUnquoteWidget extends AppWidgetProvider {
    private static volatile boolean receiversRegistered = false;
    @Nullable
    private QuoteUnquoteModel quoteUnquoteModel;
    @NonNull
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
    }

    @Override
    public void onUpdate(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] widgetIds) {
        super.onUpdate(context, appWidgetManager, widgetIds);

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
                    onReceiveActivityFinishedConfiguration(context, widgetId, appWidgetManager, eventDailyAlarm);
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
            if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)
                    || !intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveAppWidgetEnabled(@NonNull final Context context, final int widgetId) {
        getQuoteUnquoteModelInstance(context).getNextQuotation(widgetId);
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
                scheduleEvent(context, widgetId, appWidgetManager);
            }
        }

        onUpdate(context, appWidgetManager, widgetIds);
    }

    private void onReceiveToolbarPressedShare(@NonNull final Context context, final int widgetId) {
        context.startActivity(IntentFactoryHelper.createIntentShare(
                context.getResources().getString(R.string.app_name),
                getQuoteUnquoteModelInstance(context).getCurrentQuotation(
                        widgetId, new ContentPreferences(widgetId, context).getContentSelection()).theQuotation()));
    }

    private void onReceiveToolbarPressedFavourite(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        int favouritesCount = getQuoteUnquoteModelInstance(context).toggleFavourite(
                widgetId,
                getQuoteUnquoteModelInstance(context).getCurrentQuotation(
                        widgetId, contentPreferences.getContentSelection()).digest);

        noMoreFavourites(context, widgetId, appWidgetManager, contentPreferences, favouritesCount);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void noMoreFavourites(@NonNull Context context, int widgetId, @NonNull AppWidgetManager appWidgetManager, ContentPreferences contentPreferences, int favouritesCount) {
        if (favouritesCount == 0 && contentPreferences.getContentSelection() != ContentSelection.ALL) {
            contentPreferences.setContentSelection(ContentSelection.ALL);
            try {
                getQuoteUnquoteModelInstance(context).setNextQuotation(widgetId, false);
            } catch (NoNextQuotationAvailableException e) {
                Timber.d(e.getMessage());
            } finally {
                onUpdate(context, appWidgetManager, new int[]{widgetId});
            }
        }
    }

    private void onReceiveToolbarPressedFirst(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager) {
        getQuoteUnquoteModelInstance(context).resetPrevious(widgetId, new ContentPreferences(widgetId, context).getContentSelection());
        getQuoteUnquoteModelInstance(context).getNextQuotation(widgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void onReceiveToolbarPressedPrevious(
            final Context context, final int widgetId, final AppWidgetManager appWidgetManager) {
        // TODO - the back button

        /*

        assertEquals(
            "d3",
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL).digest
        )

            quoteUnquoteModelDouble.getPreviousQuotation(
                WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3"
            )?.digest



            databaseRepositoryDouble.markAsCurrent(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL, "d3")

         */

        // TODO - the counts somehow need to be displayed - maybe a relative_position field in current

        // TODO - in Model minimise use of calling other threaded methods, instead just call databaseRepoistory direct
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
        try {
            getQuoteUnquoteModelInstance(context).setNextQuotation(widgetId, randomNext);

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
        } catch (NoNextQuotationAvailableException e) {
            Timber.d(e.toString());
        }
    }

    private void onReceiveDailyAlarm(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final EventDailyAlarm scheduleDailyAlarm) {
        scheduleDailyAlarm.setDailyAlarm();
        scheduleEvent(context, widgetId, appWidgetManager);
    }

    private void scheduleEvent(@NonNull Context context, int widgetId, @NonNull AppWidgetManager appWidgetManager) {
        ContentSelection contentSelection = new ContentPreferences(widgetId, context).getContentSelection();
        EventPreferences eventPreferences = new EventPreferences(widgetId, context);

        try {
            getQuoteUnquoteModelInstance(context).setNextQuotation(widgetId, eventPreferences.getEventNextRandom());

            if (eventPreferences.getEventDisplayWidgetAndNotification()) {
                notificationHelper.displayNotification(
                        context, getQuoteUnquoteModelInstance(context).getCurrentQuotation(widgetId, contentSelection));
            }
        } catch (NoNextQuotationAvailableException e) {
            ToastHelper.makeToast(context, context.getString(R.string.widget_button_next_no_more), Toast.LENGTH_LONG);
        }
    }

    private void onReceiveActivityFinishedConfiguration(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final EventDailyAlarm eventDailyAlarm) {
        getQuoteUnquoteModelInstance(context).getNextQuotation(widgetId);
        eventDailyAlarm.setDailyAlarm();
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation);
    }

    private void setTransparency(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        final int seekBarValue = new AppearancePreferences(widgetId, context).getAppearanceTransparency();
        Timber.d("%d", widgetId);

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

    private void setHeartColour(
            @NonNull final Context context,
            final int widgetId,
            @NonNull final RemoteViews remoteViews) {
        Timber.d("%d", widgetId);

        ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        final QuotationEntity quotationEntity = getQuoteUnquoteModelInstance(context).getCurrentQuotation(
                widgetId, contentPreferences.getContentSelection());

        if (quotationEntity != null) {
            if (getQuoteUnquoteModelInstance(context).isFavourite(widgetId, quotationEntity.digest)) {
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
