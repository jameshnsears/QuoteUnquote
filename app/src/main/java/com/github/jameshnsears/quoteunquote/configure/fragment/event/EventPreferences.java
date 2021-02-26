package com.github.jameshnsears.quoteunquote.configure.fragment.event;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class EventPreferences extends PreferencesFacade {
    private static String eventNextRandom = "EVENT_NEXT_RANDOM";
    private static String eventNextSequential = "EVENT_NEXT_SEQUENTIAL";
    private static String eventDisplayWidget = "EVENT_DISPLAY_WIDGET";
    private static String eventDisplayWidgetAndNotification = "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION";
    private static String eventDaily = "EVENT_DAILY";
    private static String eventDeviceUnlock = "EVENT_DEVICE_UNLOCK";
    private static String eventDailyMinute = "EVENT_DAILY_MINUTE";
    private static String eventDailyHour = "EVENT_DAILY_HOUR";

    public EventPreferences(final int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getEventNextRandom() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventNextRandom), true);
    }

    public void setEventNextRandom(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventNextRandom), value);
    }

    public boolean getEventNextSequential() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventNextSequential), false);
    }

    public void setEventNextSequential(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventNextSequential), value);
    }

    public boolean getEventDisplayWidget() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventDisplayWidget), true);
    }

    public void setEventdisplayWidget(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDisplayWidget), value);
    }

    public boolean getEventDisplayWidgetAndNotification() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventDisplayWidgetAndNotification), false);
    }

    public void setEventdisplayWidgetAndNotification(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDisplayWidgetAndNotification), value);
    }

    public boolean getEventDaily() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventDaily), false);
    }

    public void setEventDaily(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDaily), value);
    }

    public boolean getEventDeviceUnlock() {
        return preferenceHelper.getPreferenceBoolean(getPreferenceKey(eventDeviceUnlock), false);
    }

    public void setEventDeviceUnlock(final boolean value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDeviceUnlock), value);
    }

    public int getEventDailyTimeMinute() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(eventDailyMinute));
    }

    public void setEventDailyTimeMinute(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDailyMinute), value);
    }

    public int getEventDailyTimeHour() {
        return preferenceHelper.getPreferenceInt(getPreferenceKey(eventDailyHour));
    }

    public void setEventDailyTimeHour(final int value) {
        preferenceHelper.setPreference(getPreferenceKey(eventDailyHour), value);
    }
}
