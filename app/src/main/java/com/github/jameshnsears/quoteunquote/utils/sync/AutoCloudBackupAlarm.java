package com.github.jameshnsears.quoteunquote.utils.sync;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class AutoCloudBackupAlarm {
    @Nullable
    protected final SyncPreferences syncPreferences;
    @NonNull
    protected final Context context;
    protected final int widgetId;

    public AutoCloudBackupAlarm(
            @NonNull final Context widgetContext, final int theWidgetId) {
        this.context = widgetContext;
        this.widgetId = theWidgetId;
        syncPreferences = new SyncPreferences(theWidgetId, widgetContext);
    }

    @SuppressLint({"MissingPermission", "ScheduleExactAlarm"})
    public void setAlarm() {
        if (syncPreferences.getAutoCloudBackup()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 60);

            final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm.ss", Locale.getDefault());
            Timber.d("autoCloudBackupAlarm.setAlarm: %s", sdf.format(calendar.getTime()));

            AlarmManager alarmManager =
                    (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent alarmPendingIntent
                    = IntentFactoryHelper.createClickPendingIntent(
                    this.context, this.widgetId, IntentFactoryHelper.AUTO_CLOUD_BACKUP_ALARM);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    alarmPendingIntent);
        }
    }

    public void resetAlarm() {
        Timber.d("autoCloudBackupAlarm.resetAlarm");

        final AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(IntentFactoryHelper.createClickPendingIntent(
                    context, widgetId, IntentFactoryHelper.AUTO_CLOUD_BACKUP_ALARM));
        }
    }
}