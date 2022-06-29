package com.github.jameshnsears.quoteunquote.configure.fragment.notifications;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class NotificationsPreferences extends PreferencesFacade {
    public static final String EVENT_NEXT_RANDOM = "EVENT_NEXT_RANDOM";
    public static final String EVENT_NEXT_SEQUENTIAL = "EVENT_NEXT_SEQUENTIAL";
    public static final String EVENT_DISPLAY_WIDGET = "EVENT_DISPLAY_WIDGET";
    public static final String EVENT_DISPLAY_WIDGET_AND_NOTIFICATION = "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION";
    public static final String EVENT_DAILY = "EVENT_DAILY";
    public static final String EVENT_DEVICE_UNLOCK = "EVENT_DEVICE_UNLOCK";
    public static final String EVENT_DAILY_MINUTE = "EVENT_DAILY_MINUTE";
    public static final String EVENT_DAILY_HOUR = "EVENT_DAILY_HOUR";

    public NotificationsPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getEventNextRandom() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_RANDOM), true);
    }

    public void setEventNextRandom(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_RANDOM), value);
    }

    public boolean getEventNextSequential() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_SEQUENTIAL), false);
    }

    public void setEventNextSequential(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_SEQUENTIAL), value);
    }

    public boolean getEventDisplayWidget() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_DISPLAY_WIDGET), true);
    }

    public void setEventDisplayWidget(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DISPLAY_WIDGET), value);
    }

    public boolean getEventDisplayWidgetAndNotification() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), false);
    }

    public void setEventdisplayWidgetAndNotification(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), value);
    }

    public boolean getEventDaily() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY), false);
    }

    public void setEventDaily(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY), value);
    }

    public boolean getEventDeviceUnlock() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_DEVICE_UNLOCK), false);
    }

    public void setEventDeviceUnlock(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DEVICE_UNLOCK), value);
    }

    public int getEventDailyTimeMinute() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY_MINUTE));
    }

    public void setEventDailyTimeMinute(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY_MINUTE), value);
    }

    public int getEventDailyTimeHour() {
        return this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY_HOUR));
    }

    public void setEventDailyTimeHour(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_DAILY_HOUR), value);
    }
}
