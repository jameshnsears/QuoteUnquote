package com.github.jameshnsears.quoteunquote.configure.fragment.event;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.util.Calendar;

import timber.log.Timber;

public final class EventDailyAlarm {
    @Nullable
    private final EventPreferences eventPreferences;
    private final Context context;
    private final int widgetId;

    public EventDailyAlarm(
            @NonNull final Context widgetContext, final int theWidgetId) {
        this.context = widgetContext;
        this.widgetId = theWidgetId;
        eventPreferences = new EventPreferences(theWidgetId, widgetContext);
    }

    public void setDailyAlarm() {
        if (eventPreferences.getEventDaily()) {

            Timber.d("widgetId=%d", widgetId);

            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    eventPreferences.getEventDailyTimeHour());
            calendar.set(
                    Calendar.MINUTE,
                    eventPreferences.getEventDailyTimeMinute());
            calendar.set(Calendar.SECOND, 0);

            // if user's time is < now then fire alarm tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis() + 1000) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            final PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.DAILY_ALARM);

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAnyExistingDailyAlarm() {
        if (!eventPreferences.getEventDaily()) {
            Timber.d("widgetId=%d", widgetId);

            final PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createIntentPending(context, widgetId, IntentFactoryHelper.DAILY_ALARM);
            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(alarmPendingIntent);
            }
        }
    }
}
