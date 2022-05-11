package com.github.jameshnsears.quoteunquote.configure.fragment.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TimePicker;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentScheduleBinding;

@Keep
public class ScheduleFragment extends FragmentCommon {
    @Nullable
    public FragmentScheduleBinding fragmentScheduleBinding;

    @Nullable
    public SchedulePreferences schedulePreferences;

    public ScheduleFragment() {
        // dark mode support
    }

    public ScheduleFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static ScheduleFragment newInstance(final int widgetId) {
        final ScheduleFragment fragment = new ScheduleFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @NonNull final ViewGroup container,
            @NonNull final Bundle savedInstanceState) {
        schedulePreferences = new SchedulePreferences(this.widgetId, this.getContext());

        fragmentScheduleBinding = FragmentScheduleBinding.inflate(getLayoutInflater());
        return fragmentScheduleBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentScheduleBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        setNext();
        setDisplay();
        setDeviceUnlock();
        setDaily();
        setDailyTime();

        createListenerNextRandom();
        createListenerNextSequential();
        createListenerDisplayWidget();
        createListenerDisplayWidgetAndNotification();
        createListenerDeviceUnlock();
        createListenerDaily();
        createListenerDailyTime();
    }

    private void setDaily() {
        final boolean booleanDaily = schedulePreferences.getEventDaily();

        fragmentScheduleBinding.checkBoxDailyAt.setChecked(booleanDaily);

        final TimePicker timePicker = fragmentScheduleBinding.timePickerDailyAt;

        timePicker.setEnabled(false);
        if (booleanDaily) {
            timePicker.setEnabled(true);
        }
    }

    private void setNext() {
        fragmentScheduleBinding.radioButtonNextRandom.setChecked(schedulePreferences.getEventNextRandom());
        fragmentScheduleBinding.radioButtonNextSequential.setChecked(schedulePreferences.getEventNextSequential());
    }

    private void setDisplay() {
        fragmentScheduleBinding.radioButtonWhereInWidget.setChecked(schedulePreferences.getEventDisplayWidget());
        fragmentScheduleBinding.radioButtonWhereAsNotification.setChecked(schedulePreferences.getEventDisplayWidgetAndNotification());
    }

    private void setDeviceUnlock() {
        fragmentScheduleBinding.checkBoxDeviceUnlock.setChecked(schedulePreferences.getEventDeviceUnlock());
    }

    private void createListenerNextRandom() {
        final RadioButton radioButtonNextRandom = fragmentScheduleBinding.radioButtonNextRandom;
        radioButtonNextRandom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventNextRandom() != isChecked) {
                schedulePreferences.setEventNextRandom(isChecked);
            }
        });
    }

    private void createListenerNextSequential() {
        final RadioButton radioButtonNextSequential = fragmentScheduleBinding.radioButtonNextSequential;
        radioButtonNextSequential.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventNextSequential() != isChecked) {
                schedulePreferences.setEventNextSequential(isChecked);
            }
        });
    }

    private void createListenerDisplayWidget() {
        final RadioButton radioButtonWhereInWidget = fragmentScheduleBinding.radioButtonWhereInWidget;
        radioButtonWhereInWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventDisplayWidget() != isChecked) {
                schedulePreferences.setEventDisplayWidget(isChecked);
            }
        });
    }

    private void createListenerDisplayWidgetAndNotification() {
        final RadioButton radioButtonWhereAsNotification = fragmentScheduleBinding.radioButtonWhereAsNotification;
        radioButtonWhereAsNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventDisplayWidgetAndNotification() != isChecked) {
                schedulePreferences.setEventdisplayWidgetAndNotification(isChecked);
            }
        });
    }

    private void createListenerDeviceUnlock() {
        final CheckBox checkBoxDeviceUnlock = fragmentScheduleBinding.checkBoxDeviceUnlock;
        checkBoxDeviceUnlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventDeviceUnlock() != isChecked) {
                schedulePreferences.setEventDeviceUnlock(isChecked);
            }
        });
    }

    private void createListenerDaily() {
        final CheckBox checkBoxDailyAt = fragmentScheduleBinding.checkBoxDailyAt;
        checkBoxDailyAt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (schedulePreferences.getEventDaily() != isChecked) {
                schedulePreferences.setEventDaily(isChecked);
            }

            final TimePicker timePicker = fragmentScheduleBinding.timePickerDailyAt;

            timePicker.setEnabled(false);
            if (isChecked) {
                timePicker.setEnabled(true);
            }
        });
    }

    private void createListenerDailyTime() {
        final TimePicker timePicker = fragmentScheduleBinding.timePickerDailyAt;
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
                    int h = timePicker.getHour();
                    if (schedulePreferences.getEventDailyTimeHour() != h) {
                        schedulePreferences.setEventDailyTimeHour(h);
                    }

                    int m = timePicker.getMinute();
                    if (schedulePreferences.getEventDailyTimeMinute() != m) {
                        schedulePreferences.setEventDailyTimeMinute(m);
                    }
                }
        );
    }

    protected void setDailyTime() {
        final TimePicker timePicker = fragmentScheduleBinding.timePickerDailyAt;

        final int hourOfDay = schedulePreferences.getEventDailyTimeHour();
        if (hourOfDay == -1) {
            schedulePreferences.setEventDailyTimeHour(6);
            timePicker.setHour(6);
        } else {
            timePicker.setHour(hourOfDay);
        }

        final int minute = schedulePreferences.getEventDailyTimeMinute();
        if (minute == -1) {
            schedulePreferences.setEventDailyTimeMinute(0);
            timePicker.setMinute(0);
        } else {
            timePicker.setMinute(minute);
        }

        timePicker.setIs24HourView(false);
    }
}
