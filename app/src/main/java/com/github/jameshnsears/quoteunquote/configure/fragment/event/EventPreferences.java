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

    public EventPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getEventNextRandom() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_NEXT_RANDOM), true);
    }

    public void setEventNextRandom(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_NEXT_RANDOM), value);
    }

    public boolean getEventNextSequential() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_NEXT_SEQUENTIAL), false);
    }

    public void setEventNextSequential(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_NEXT_SEQUENTIAL), value);
    }

    public boolean getEventDisplayWidget() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_DISPLAY_WIDGET), true);
    }

    public void setEventDisplayWidget(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DISPLAY_WIDGET), value);
    }

    public boolean getEventDisplayWidgetAndNotification() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), false);
    }

    public void setEventdisplayWidgetAndNotification(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), value);
    }

    public boolean getEventDaily() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_DAILY), false);
    }

    public void setEventDaily(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DAILY), value);
    }

    public boolean getEventDeviceUnlock() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(EventPreferences.EVENT_DEVICE_UNLOCK), false);
    }

    public void setEventDeviceUnlock(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DEVICE_UNLOCK), value);
    }

    public int getEventDailyTimeMinute() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(EventPreferences.EVENT_DAILY_MINUTE));
    }

    public void setEventDailyTimeMinute(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DAILY_MINUTE), value);
    }

    public int getEventDailyTimeHour() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(EventPreferences.EVENT_DAILY_HOUR));
    }

    public void setEventDailyTimeHour(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(EventPreferences.EVENT_DAILY_HOUR), value);
    }

    public void performMigration() {
        Map<String, ?> sharedPreferenceEntries
                = this.applicationContext.getSharedPreferences("QuoteUnquote-Preferences", Context.MODE_PRIVATE).getAll();

        for (Map.Entry<String, ?> entry : sharedPreferenceEntries.entrySet()) {
            this.widgetId = Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf(":")));

            if (entry.getKey().contains("FragmentEvent:checkBoxDeviceUnlock")) {
                final boolean checkBoxDeviceUnlock = (Boolean) entry.getValue();
                Timber.d("%d: checkBoxDeviceUnlock=%b", this.widgetId, checkBoxDeviceUnlock);
                this.setEventDeviceUnlock(checkBoxDeviceUnlock);
            }

            if (entry.getKey().contains("FragmentEvent:checkBoxDailyAt")) {
                final boolean checkBoxDailyAt = (Boolean) entry.getValue();
                Timber.d("%d: checkBoxDailyAt=%b", this.widgetId, checkBoxDailyAt);
                this.setEventDaily(checkBoxDailyAt);
            }

            if (entry.getKey().contains("FragmentEvent:timePickerDailyAt:hourOfDay")) {
                final int hourOfDay = (Integer) entry.getValue();
                Timber.d("%d: hourOfDay=%d", this.widgetId, hourOfDay);
                this.setEventDailyTimeHour(hourOfDay);
            }

            if (entry.getKey().contains("FragmentEvent:timePickerDailyAt:minute")) {
                final int minute = (Integer) entry.getValue();
                Timber.d("%d: minute=%d", this.widgetId, minute);
                this.setEventDailyTimeMinute(minute);
            }
        }
    }
}
