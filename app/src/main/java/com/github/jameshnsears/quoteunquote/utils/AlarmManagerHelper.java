package com.github.jameshnsears.quoteunquote.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;

import timber.log.Timber;

public class AlarmManagerHelper {
    public static boolean canScheduleExactAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }

        try {
            return alarmManager.canScheduleExactAlarms();
        } catch (NoSuchMethodError e) {
            Timber.e(e, "Liar API: canScheduleExactAlarms missing on API %d", Build.VERSION.SDK_INT);
            return true;
        }
    }
}
