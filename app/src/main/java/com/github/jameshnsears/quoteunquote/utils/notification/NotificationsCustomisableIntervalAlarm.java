package com.github.jameshnsears.quoteunquote.utils.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class NotificationsCustomisableIntervalAlarm extends NotificationsDailyAlarm {
    int nextAlarmHour;
    boolean nextAlarmDay;
    public NotificationsCustomisableIntervalAlarm(
            @NonNull Context widgetContext, int theWidgetId) {
        super(widgetContext, theWidgetId);
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setAlarm() {
        if (notificationsPreferences.getCustomisableInterval()) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));

            int from = this.notificationsPreferences.getCustomisableIntervalHourFrom();
            int to = this.notificationsPreferences.getCustomisableIntervalHourTo();
            int hours = this.notificationsPreferences.getCustomisableIntervalHours();

            if (currentHour() < from) {
                calendar.set(Calendar.HOUR_OF_DAY, from);
                nextAlarmHour = from;
                nextAlarmDay = false;
            }
            else if (currentHour() > to) {
                calendar.set(Calendar.HOUR_OF_DAY, from);
                nextAlarmHour = from;
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                nextAlarmDay = true;
            }
            else {
                for (int alarmHour = from ; alarmHour <= to; alarmHour += hours) {
                    if (alarmHour > currentHour()) {
                        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
                        nextAlarmHour = alarmHour;
                        nextAlarmDay = false;
                        break;
                    }
                }

                if (nextAlarmHour == 0) {
                    calendar.set(Calendar.HOUR_OF_DAY, from);
                    nextAlarmHour = from;
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    nextAlarmDay = true;
                }
            }

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss", Locale.getDefault());
            Timber.d("customisableIntervalAlarm: %s", sdf.format(calendar.getTime()));
            Timber.d("nextAlarmHour: %d", this.nextAlarmHour);
            Timber.d("nextAlarmDay: %b", this.nextAlarmDay);

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(
                    this.context, this.widgetId, IntentFactoryHelper.CUSTOMISABLE_INTERVAL_ALARM);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAlarm() {
        if (!this.notificationsPreferences.getCustomisableInterval()) {
            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            if (null != alarmManager) {
                alarmManager.cancel(IntentFactoryHelper.createClickPendingIntent(
                        this.context, this.widgetId, IntentFactoryHelper.CUSTOMISABLE_INTERVAL_ALARM));
            }
        }
    }

    public int currentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}
