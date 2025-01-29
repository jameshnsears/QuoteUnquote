package com.github.jameshnsears.quoteunquote.configure.fragment.notifications;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class NotificationsPreferences extends PreferencesFacade {
    public static final String EVENT_NEXT_RANDOM = "EVENT_NEXT_RANDOM";
    public static final String EVENT_NEXT_SEQUENTIAL = "EVENT_NEXT_SEQUENTIAL";
    public static final String EVENT_DISPLAY_WIDGET = "EVENT_DISPLAY_WIDGET";
    public static final String EVENT_DISPLAY_WIDGET_AND_NOTIFICATION = "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION";
    public static final String EVENT_EXCLUDE_SOURCE_FROM_NOTIFICATION = "EVENT_EXCLUDE_SOURCE_FROM_NOTIFICATION";
    public static final String EVENT_DAILY = "EVENT_DAILY";
    public static final String EVENT_DEVICE_UNLOCK = "EVENT_DEVICE_UNLOCK";
    public static final String EVENT_DAILY_MINUTE = "EVENT_DAILY_MINUTE";
    public static final String EVENT_DAILY_HOUR = "EVENT_DAILY_HOUR";
    public static final String EVENT_CUSTOMISABLE_INTERVAL = "EVENT_CUSTOMISABLE_INTERVAL";
    public static final String EVENT_CUSTOMISABLE_INTERVAL_HOUR_FROM = "EVENT_CUSTOMISABLE_INTERVAL_HOUR_FROM";
    public static final String EVENT_CUSTOMISABLE_INTERVAL_HOUR_TO = "EVENT_CUSTOMISABLE_INTERVAL_HOUR_TO";
    public static final String EVENT_CUSTOMISABLE_INTERVAL_HOURS = "EVENT_CUSTOMISABLE_INTERVAL_HOURS";

    public static final String EVENT_TTS_UK = "EVENT_TTS_UK";
    public static final String EVENT_TTS_SYSTEM = "EVENT_TTS_SYSTEM";

    public NotificationsPreferences(int widgetId, @NonNull Context applicationContext) {
        super(widgetId, applicationContext);
    }

    public boolean getEventTtsUk() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_TTS_UK), false);
    }

    public void setEventTtsUk(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_TTS_UK), value);
    }

    public boolean getEventTtsSystem() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_TTS_SYSTEM), false);
    }

    //////////////////

    public void setEventTtsSystem(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_TTS_SYSTEM), value);
    }

    public int getCustomisableIntervalHours() {
        int hours = this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOURS));
        if (hours == -1) {
            return 1;
        } else {
            return hours;
        }
    }

    public void setCustomisableIntervalHours(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOURS), value);
    }

    public int getCustomisableIntervalHourTo() {
        int hour = this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOUR_TO));
        if (hour == -1) {
            return 23;
        } else {
            return hour;
        }
    }

    public void setCustomisableIntervalHourTo(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOUR_TO), value);
    }

    public int getCustomisableIntervalHourFrom() {
        int hour = this.preferenceHelper.getPreferenceInt(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOUR_FROM));
        if (hour == -1) {
            return 0;
        } else {
            return hour;
        }
    }

    public void setCustomisableIntervalHourFrom(int value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL_HOUR_FROM), value);
    }

    public boolean getEventNextRandom() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_RANDOM), false);
    }

    public void setEventNextRandom(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_RANDOM), value);
    }

    public boolean getEventNextSequential() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_NEXT_SEQUENTIAL), true);
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

    public boolean getExcludeSourceFromNotification() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_EXCLUDE_SOURCE_FROM_NOTIFICATION), false);
    }

    public void setExcludeSourceFromNotification(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_EXCLUDE_SOURCE_FROM_NOTIFICATION), value);
    }

    public boolean getEventDisplayWidgetAndNotification() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_DISPLAY_WIDGET_AND_NOTIFICATION), false);
    }

    public void setEventDisplayWidgetAndNotification(boolean value) {
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

    public boolean getCustomisableInterval() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL), false);
    }

    public void setCustomisableInterval(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(NotificationsPreferences.EVENT_CUSTOMISABLE_INTERVAL), value);
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
