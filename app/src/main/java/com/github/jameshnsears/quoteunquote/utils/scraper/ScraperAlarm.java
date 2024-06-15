package com.github.jameshnsears.quoteunquote.utils.scraper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class ScraperAlarm {
    @Nullable
    protected final QuotationsPreferences quotationsPreferences;
    @NonNull
    protected final Context context;
    protected final int widgetId;

    public ScraperAlarm(
            @NonNull final Context widgetContext, final int theWidgetId) {
        this.context = widgetContext;
        this.widgetId = theWidgetId;
        quotationsPreferences = new QuotationsPreferences(theWidgetId, widgetContext);
    }

    @SuppressLint({"MissingPermission", "ScheduleExactAlarm"})
    public void setAlarm() {
        if (quotationsPreferences.getDatabaseExternalWeb()) {

            Calendar calendar = Calendar.getInstance();
            int currentMinute = calendar.get(Calendar.MINUTE);

            if (currentMinute == 0 || currentMinute == 30) {
                calendar.add(Calendar.MINUTE, 30);
            } else {
                calendar.add(Calendar.MINUTE, 60 - currentMinute);
            }
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss", Locale.getDefault());
            Timber.d("scraperAlarm: %s", sdf.format(calendar.getTime()));

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(
                    this.context, this.widgetId, IntentFactoryHelper.SCRAPER_ALARM);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAlarm() {
        if (!quotationsPreferences.getDatabaseExternalWeb()) {

            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(IntentFactoryHelper.createClickPendingIntent(
                        context, widgetId, IntentFactoryHelper.SCRAPER_ALARM));
            }
        }
    }
}
