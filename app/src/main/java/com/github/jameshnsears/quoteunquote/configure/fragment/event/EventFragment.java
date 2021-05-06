package com.github.jameshnsears.quoteunquote.configure.fragment.event;

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
import com.github.jameshnsears.quoteunquote.databinding.FragmentEventBinding;

@Keep
public class EventFragment extends FragmentCommon {
    @Nullable
    public FragmentEventBinding fragmentEventBinding;

    @Nullable
    public EventPreferences eventPreferences;

    public EventFragment() {
        // dark mode support
    }

    public EventFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static EventFragment newInstance(int widgetId) {
        EventFragment fragment = new EventFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        this.eventPreferences = new EventPreferences(widgetId, getContext());

        this.fragmentEventBinding = FragmentEventBinding.inflate(this.getLayoutInflater());
        return this.fragmentEventBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentEventBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull View view, Bundle savedInstanceState) {
        this.setNext();
        this.setDisplay();
        this.setDeviceUnlock();
        this.setDaily();
        this.setDailyTime();

        this.createListenerNextRandom();
        this.createListenerNextSequential();
        this.createListenerDisplayWidget();
        this.createListenerDisplayWidgetAndNotification();
        this.createListenerDeviceUnlock();
        this.createListenerDaily();
        this.createListenerDailyTime();
    }

    private void setDaily() {
        boolean booleanDaily = this.eventPreferences.getEventDaily();

        this.fragmentEventBinding.checkBoxDailyAt.setChecked(booleanDaily);

        TimePicker timePicker = this.fragmentEventBinding.timePickerDailyAt;

        timePicker.setEnabled(false);
        if (booleanDaily) {
            timePicker.setEnabled(true);
        }
    }

    private void setNext() {
        this.fragmentEventBinding.radioButtonNextRandom.setChecked(this.eventPreferences.getEventNextRandom());
        this.fragmentEventBinding.radioButtonNextSequential.setChecked(this.eventPreferences.getEventNextSequential());
    }

    private void setDisplay() {
        this.fragmentEventBinding.radioButtonWhereInWidget.setChecked(this.eventPreferences.getEventDisplayWidget());
        this.fragmentEventBinding.radioButtonWhereAsNotification.setChecked(this.eventPreferences.getEventDisplayWidgetAndNotification());
    }

    private void setDeviceUnlock() {
        this.fragmentEventBinding.checkBoxDeviceUnlock.setChecked(this.eventPreferences.getEventDeviceUnlock());
    }

    private void createListenerNextRandom() {
        RadioButton radioButtonNextRandom = this.fragmentEventBinding.radioButtonNextRandom;
        radioButtonNextRandom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventNextRandom() != isChecked) {
                this.eventPreferences.setEventNextRandom(isChecked);
            }
        });
    }

    private void createListenerNextSequential() {
        RadioButton radioButtonNextSequential = this.fragmentEventBinding.radioButtonNextSequential;
        radioButtonNextSequential.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventNextSequential() != isChecked) {
                this.eventPreferences.setEventNextSequential(isChecked);
            }
        });
    }

    private void createListenerDisplayWidget() {
        RadioButton radioButtonWhereInWidget = this.fragmentEventBinding.radioButtonWhereInWidget;
        radioButtonWhereInWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventDisplayWidget() != isChecked) {
                this.eventPreferences.setEventDisplayWidget(isChecked);
            }
        });
    }

    private void createListenerDisplayWidgetAndNotification() {
        RadioButton radioButtonWhereAsNotification = this.fragmentEventBinding.radioButtonWhereAsNotification;
        radioButtonWhereAsNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventDisplayWidgetAndNotification() != isChecked) {
                this.eventPreferences.setEventdisplayWidgetAndNotification(isChecked);
            }
        });
    }

    private void createListenerDeviceUnlock() {
        CheckBox checkBoxDeviceUnlock = this.fragmentEventBinding.checkBoxDeviceUnlock;
        checkBoxDeviceUnlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventDeviceUnlock() != isChecked) {
                this.eventPreferences.setEventDeviceUnlock(isChecked);
            }
        });
    }

    private void createListenerDaily() {
        CheckBox checkBoxDailyAt = this.fragmentEventBinding.checkBoxDailyAt;
        checkBoxDailyAt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.eventPreferences.getEventDaily() != isChecked) {
                this.eventPreferences.setEventDaily(isChecked);
            }

            TimePicker timePicker = this.fragmentEventBinding.timePickerDailyAt;

            timePicker.setEnabled(false);
            if (isChecked) {
                timePicker.setEnabled(true);
            }
        });
    }

    private void createListenerDailyTime() {
        TimePicker timePicker = this.fragmentEventBinding.timePickerDailyAt;
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
                    final int h = timePicker.getHour();
                    if (this.eventPreferences.getEventDailyTimeHour() != h) {
                        this.eventPreferences.setEventDailyTimeHour(h);
                    }

                    final int m = timePicker.getMinute();
                    if (this.eventPreferences.getEventDailyTimeMinute() != m) {
                        this.eventPreferences.setEventDailyTimeMinute(m);
                    }
                }
        );
    }

    protected void setDailyTime() {
        TimePicker timePicker = this.fragmentEventBinding.timePickerDailyAt;

        int hourOfDay = this.eventPreferences.getEventDailyTimeHour();
        if (hourOfDay == -1) {
            this.eventPreferences.setEventDailyTimeHour(6);
            timePicker.setHour(6);
        } else {
            timePicker.setHour(hourOfDay);
        }

        int minute = this.eventPreferences.getEventDailyTimeMinute();
        if (minute == -1) {
            this.eventPreferences.setEventDailyTimeMinute(0);
            timePicker.setMinute(0);
        } else {
            timePicker.setMinute(minute);
        }

        timePicker.setIs24HourView(false);
    }
}
