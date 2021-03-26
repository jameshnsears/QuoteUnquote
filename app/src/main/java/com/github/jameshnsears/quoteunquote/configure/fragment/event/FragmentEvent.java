package com.github.jameshnsears.quoteunquote.configure.fragment.event;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentEventBinding;
import com.github.jameshnsears.quoteunquote.utils.Preferences;


public final class FragmentEvent extends FragmentCommon {
    private static final String LOG_TAG = FragmentEvent.class.getSimpleName();

    public FragmentEventBinding fragmentEventBinding;

    private FragmentEvent(final int widgetId) {
        super(LOG_TAG, widgetId);
    }

    public static FragmentEvent newInstance(final int widgetId) {
        final FragmentEvent fragment = new FragmentEvent(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        fragmentEventBinding = FragmentEventBinding.inflate(getLayoutInflater());
        return fragmentEventBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentEventBinding = null;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        setCheckBoxDeviceUnlockViaPreference();
        setCheckBoxDailyAtViaSharedPreference();
        setTimePickerViaSharedPreference();

        createCheckBoxDeviceUnlockListener();
        createCheckBoxDailyAtListener();
        createTimePickerListener();
    }

    private void setCheckBoxDailyAtViaSharedPreference() {
        final boolean booleanDailyAt = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DAILY_AT);

        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DAILY_AT, booleanDailyAt);

        final CheckBox checkBoxDailyAt = fragmentEventBinding.checkBoxDailyAt;
        checkBoxDailyAt.setChecked(booleanDailyAt);

        final TimePicker timePicker = fragmentEventBinding.timePickerDailyAt;

        timePicker.setEnabled(false);
        if (booleanDailyAt) {
            timePicker.setEnabled(true);
        }
    }

    private void setCheckBoxDeviceUnlockViaPreference() {
        final boolean booleanDeviceUnlock = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DEVICE_UNLOCK);

        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DEVICE_UNLOCK, booleanDeviceUnlock);

        final CheckBox checkBoxDeviceUnlock = fragmentEventBinding.checkBoxDeviceUnlock;
        checkBoxDeviceUnlock.setChecked(booleanDeviceUnlock);
    }

    private void createCheckBoxDeviceUnlockListener() {
        final CheckBox checkBoxDeviceUnlock = fragmentEventBinding.checkBoxDeviceUnlock;
        checkBoxDeviceUnlock.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DEVICE_UNLOCK, isChecked));
    }

    private void createCheckBoxDailyAtListener() {
        final CheckBox checkBoxDailyAt = fragmentEventBinding.checkBoxDailyAt;
        checkBoxDailyAt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_EVENT, Preferences.CHECK_BOX_DAILY_AT, isChecked);

            final TimePicker timePicker = fragmentEventBinding.timePickerDailyAt;

            timePicker.setEnabled(false);
            if (isChecked) {
                timePicker.setEnabled(true);
            }
        });
    }

    private void createTimePickerListener() {
        final TimePicker timePicker = fragmentEventBinding.timePickerDailyAt;
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> saveSharedPreferenceTimePicker(timePicker.getHour(), timePicker.getMinute()));
    }

    private void saveSharedPreferenceTimePicker(final int hourOfDay, final int minute) {
        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_HOUR, hourOfDay);

        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_MINUTE, minute);
    }

    private void setTimePickerViaSharedPreference() {
        final TimePicker timePicker = fragmentEventBinding.timePickerDailyAt;

        final int hourOfDay = preferences.getSharedPreferenceInt(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_HOUR);
        if (hourOfDay == -1) {
            timePicker.setHour(6);
        } else {
            timePicker.setHour(hourOfDay);
        }
        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_HOUR, hourOfDay);

        final int minute = preferences.getSharedPreferenceInt(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_MINUTE);
        if (minute == -1) {
            timePicker.setMinute(0);
        } else {
            timePicker.setMinute(minute);
        }
        preferences.setSharedPreference(
                Preferences.FRAGMENT_EVENT, Preferences.TIME_PICKER_MINUTE, minute);

        timePicker.setIs24HourView(false);
    }
}
