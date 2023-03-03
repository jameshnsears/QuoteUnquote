package com.github.jameshnsears.quoteunquote.utils.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class NotificationsDailyAlarm {
    @Nullable
    protected final NotificationsPreferences notificationsPreferences;
    @NonNull
    protected final Context context;
    protected final int widgetId;

    public NotificationsDailyAlarm(
            @NonNull final Context widgetContext, final int theWidgetId) {
        this.context = widgetContext;
        this.widgetId = theWidgetId;
        notificationsPreferences = new NotificationsPreferences(theWidgetId, widgetContext);
    }

    @SuppressLint("MissingPermission")
    public void setAlarm() {
        if (notificationsPreferences.getEventDaily()) {

            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    notificationsPreferences.getEventDailyTimeHour());
            calendar.set(
                    Calendar.MINUTE,
                    notificationsPreferences.getEventDailyTimeMinute());
            calendar.set(Calendar.SECOND, 0);

            // if user's time is < now then fire alarm tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis() + 1000) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss", Locale.getDefault());
            Timber.d("dailyAlarm: %s", sdf.format(calendar.getTime()));

            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            final PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(
                            context, widgetId, IntentFactoryHelper.DAILY_ALARM);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAlarm() {
        if (!notificationsPreferences.getEventDaily()) {

            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(IntentFactoryHelper.createClickPendingIntent(
                        context, widgetId, IntentFactoryHelper.BIHOURLY_ALARM));
            }
        }
    }
}
