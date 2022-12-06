package com.github.jameshnsears.quoteunquote.utils.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class NotificationsBihourlyAlarm extends NotificationsDailyAlarm {
    public NotificationsBihourlyAlarm(
            @NonNull Context widgetContext, int theWidgetId) {
        super(widgetContext, theWidgetId);
    }

    @SuppressLint("MissingPermission")
    public void setAlarm() {
        if (this.notificationsPreferences.getEventBihourly()) {

            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            if (currentHour % 2 == 0) {
                calendar.add(Calendar.HOUR_OF_DAY, 2);
            } else {
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
            }
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss", Locale.getDefault());
            Timber.d("biHourlyAlarm: %s", sdf.format(calendar.getTime()));

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(
                    this.context, this.widgetId, IntentFactoryHelper.BIHOURLY_ALARM);

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAlarm() {
        if (!this.notificationsPreferences.getEventDaily()) {

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(IntentFactoryHelper.createClickPendingIntent(
                        this.context, this.widgetId, IntentFactoryHelper.BIHOURLY_ALARM));
            }
        }
    }
}
