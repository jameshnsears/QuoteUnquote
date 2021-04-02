package com.github.jameshnsears.quoteunquote.configure.fragment.event;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

import java.util.Map;

import timber.log.Timber;

public class EventPreferences extends PreferencesFacade {
    private static final String EVENT_NEXT_RANDOM = "EVENT_NEXT_RANDOM";
    private static final String EVENT_NEXT_SEQUENTIAL = "EVENT_NEXT_SEQUENTIAL";
    private static final String EVENT_DISPLAY_WIDGET = "EVENT_DISPLAY_WIDGET";
    private static final String EVENT_DISPLAY_WIDGET_AND_NOTIFICATION = "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION";
    private static final String EVENT_DAILY = "EVENT_DAILY";
    private static final String EVENT_DEVICE_UNLOCK = "EVENT_DEVICE_UNLOCK";
    private static final String EVENT_DAILY_MINUTE = "EVENT_DAILY_MINUTE";
    private static final String EVENT_DAILY_HOUR = "EVENT_DAILY_HOUR";

    public EventPreferences(final int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getEventNextRandom() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_NEXT_RANDOM), true);
    }

    public void setEventNextRandom(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_NEXT_RANDOM), value);
    }

    public boolean getEventNextSequential() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_NEXT_SEQUENTIAL), false);
    }

    public void setEventNextSequential(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_NEXT_SEQUENTIAL), value);
    }

    public boolean getEventDisplayWidget() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_DISPLAY_WIDGET), true);
    }

    public void setEventDisplayWidget(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DISPLAY_WIDGET), value);
    }

    public boolean getEventDisplayWidgetAndNotification() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), false);
    }

    public void setEventdisplayWidgetAndNotification(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), value);
    }

    public boolean getEventDaily() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_DAILY), false);
    }

    public void setEventDaily(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DAILY), value);
    }

    public boolean getEventDeviceUnlock() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(EVENT_DEVICE_UNLOCK), false);
    }

    public void setEventDeviceUnlock(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DEVICE_UNLOCK), value);
    }

    public int getEventDailyTimeMinute() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(EVENT_DAILY_MINUTE));
    }

    public void setEventDailyTimeMinute(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DAILY_MINUTE), value);
    }

    public int getEventDailyTimeHour() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(EVENT_DAILY_HOUR));
    }

    public void setEventDailyTimeHour(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(EVENT_DAILY_HOUR), value);
    }

    public void performMigration() {
        final Map<String, ?> sharedPreferenceEntries
                = applicationContext.getSharedPreferences("QuoteUnquote-Preferences", Context.MODE_PRIVATE).getAll();

        for (final Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            widgetId = Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf(":")));

            if (entry.getKey().contains("FragmentEvent:checkBoxDeviceUnlock")) {
                boolean checkBoxDeviceUnlock = (Boolean) entry.getValue();
                Timber.d("%d: checkBoxDeviceUnlock=%b", widgetId, checkBoxDeviceUnlock);
                setEventDeviceUnlock(checkBoxDeviceUnlock);
            }

            if (entry.getKey().contains("FragmentEvent:checkBoxDailyAt")) {
                boolean checkBoxDailyAt = (Boolean) entry.getValue();
                Timber.d("%d: checkBoxDailyAt=%b", widgetId, checkBoxDailyAt);
                setEventDaily(checkBoxDailyAt);
            }

            if (entry.getKey().contains("FragmentEvent:timePickerDailyAt:hourOfDay")) {
                int hourOfDay = (Integer) entry.getValue();
                Timber.d("%d: hourOfDay=%d", widgetId, hourOfDay);
                setEventDailyTimeHour(hourOfDay);
            }

            if (entry.getKey().contains("FragmentEvent:timePickerDailyAt:minute")) {
                int minute = (Integer) entry.getValue();
                Timber.d("%d: minute=%d", widgetId, minute);
                setEventDailyTimeMinute(minute);
            }
        }
    }
}
