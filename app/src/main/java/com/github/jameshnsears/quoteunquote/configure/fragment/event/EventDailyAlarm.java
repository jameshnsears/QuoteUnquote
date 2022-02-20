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
    @NonNull
    private final Context context;
    private final int widgetId;

    public EventDailyAlarm(
            @NonNull Context widgetContext, int theWidgetId) {
        context = widgetContext;
        widgetId = theWidgetId;
        this.eventPreferences = new EventPreferences(theWidgetId, widgetContext);
    }

    public void setDailyAlarm() {
        if (this.eventPreferences.getEventDaily()) {
            Timber.d("%d", this.widgetId);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    this.eventPreferences.getEventDailyTimeHour());
            calendar.set(
                    Calendar.MINUTE,
                    this.eventPreferences.getEventDailyTimeMinute());
            calendar.set(Calendar.SECOND, 0);

            // if user's time is < now then fire alarm tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis() + 1000) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(this.context, this.widgetId, IntentFactoryHelper.DAILY_ALARM);

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAnyExistingDailyAlarm() {
        if (!this.eventPreferences.getEventDaily()) {
            Timber.d("%d", this.widgetId);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(this.context, this.widgetId, IntentFactoryHelper.DAILY_ALARM);
            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(alarmPendingIntent);
            }
        }
    }
}
