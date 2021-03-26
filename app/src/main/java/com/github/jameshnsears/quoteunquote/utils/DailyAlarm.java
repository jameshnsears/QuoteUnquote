package com.github.jameshnsears.quoteunquote.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;

public class DailyAlarm {
    private static final String LOG_TAG = DailyAlarm.class.getSimpleName();

    private final Preferences preferences;
    private final Context context;
    private final int widgetId;

    public DailyAlarm(final Context context, final int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        preferences = new Preferences(widgetId, context);
    }

    public void setDailyAlarm() {
        if (preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DAILY_AT)) {

            Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                    new Object() {
                    }.getClass().getEnclosingMethod().getName()));

            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    preferences.getSharedPreferenceInt(Preferences.FRAGMENT_EVENT, "timePickerDailyAt:hourOfDay"));
            calendar.set(
                    Calendar.MINUTE,
                    preferences.getSharedPreferenceInt(Preferences.FRAGMENT_EVENT, "timePickerDailyAt:minute"));
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
        if (!preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DAILY_AT)) {
            Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                    new Object() {
                    }.getClass().getEnclosingMethod().getName()));

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
