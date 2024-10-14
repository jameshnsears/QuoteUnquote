package com.github.jameshnsears.quoteunquote.configure.fragment.notifications;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentNotificationsBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

@Keep
public class NotificationsFragment extends FragmentCommon {
    @Nullable
    public FragmentNotificationsBinding fragmentNotificationsBinding;

    @Nullable
    public NotificationsPreferences notificationsPreferences;

    private ActivityResultLauncher<String> requestPermissionLauncherNotifications =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    QuoteUnquoteWidget.notificationPermissionDeniedCount += 1;
                    if (QuoteUnquoteWidget.notificationPermissionDeniedCount >= 3) {
                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_notifications_notification_permission),
                                Toast.LENGTH_LONG).show();
                    }

                    fragmentNotificationsBinding.radioButtonWhereInWidget.performClick();
                } else {
                    notificationsPreferences.setEventDisplayWidget(false);
                    notificationsPreferences.setEventDisplayWidgetAndNotification(true);
                    fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);
                    fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(true);
                    fragmentNotificationsBinding.textViewNotificationSizeWarning1.setEnabled(true);
                }

                ConfigureActivity.launcherInvoked = false;
            });

    private ActivityResultLauncher<Intent> requestExactAlarmLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Boolean isChecked = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager.canScheduleExactAlarms()) {
                        isChecked = true;
                    }
                } else {
                    isChecked = true;
                }

                notificationsPreferences.setCustomisableInterval(isChecked);
                setCustomisableInterval();
            }
    );

    public NotificationsFragment() {
        // dark mode support
    }

    public NotificationsFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static NotificationsFragment newInstance(final int widgetId) {
        final NotificationsFragment fragment = new NotificationsFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @NonNull final ViewGroup container,
            @NonNull final Bundle savedInstanceState) {
        notificationsPreferences = new NotificationsPreferences(this.widgetId, this.getContext());

        fragmentNotificationsBinding = FragmentNotificationsBinding.inflate(getLayoutInflater());
        return fragmentNotificationsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentNotificationsBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        setAction();

        setDisplay();

        setDeviceUnlock();

        handleSpecialPermissionForExactAlarm();

        setCustomisableInterval();
        setCustomisableIntervalSliderHours();
        setCustomisableIntervalSpinnerHours(
                notificationsPreferences.getCustomisableIntervalHourTo() - notificationsPreferences.getCustomisableIntervalHourFrom()
        );

        setSpecificTime();

        createListenerNextRandom();
        createListenerNextSequential();

        createListenerDisplayRadioGroup();
        createListenerExcludeSourceFromNotification();

        createListenerDeviceUnlock();

        createListenerCustomisableInterval();
        createListenerCustomisableIntervalSliderHours();
        createListenerCustomisableIntervalSpinnerHours();

        createListenerSpecificTime();
        createListenerDaily();
    }

    private void handleSpecialPermissionForExactAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            fragmentNotificationsBinding.textViewExactTimeWarningDivider.setVisibility(VISIBLE);
            fragmentNotificationsBinding.textViewExactTimeWarningInfo.setVisibility(VISIBLE);
            fragmentNotificationsBinding.textViewExactTimeWarning.setVisibility(VISIBLE);

            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                fragmentNotificationsBinding.checkBoxCustomisableInterval.setChecked(false);
                notificationsPreferences.setCustomisableInterval(false);

                fragmentNotificationsBinding.checkBoxDailyAt.setChecked(false);
                notificationsPreferences.setEventDaily(false);
            } else {
                handleSpecialPermissionForExactAlarmCommon();
            }
        } else {
            handleSpecialPermissionForExactAlarmCommon();

            fragmentNotificationsBinding.textViewExactTimeWarningDivider.setVisibility(View.GONE);
            fragmentNotificationsBinding.textViewExactTimeWarningInfo.setVisibility(View.GONE);
            fragmentNotificationsBinding.textViewExactTimeWarning.setVisibility(View.GONE);
        }
    }

    private void handleSpecialPermissionForExactAlarmCommon() {
        if (fragmentNotificationsBinding.checkBoxCustomisableInterval.isChecked()) {
            notificationsPreferences.setCustomisableInterval(true);
        }

        if (fragmentNotificationsBinding.checkBoxDailyAt.isChecked()) {
            notificationsPreferences.setEventDaily(true);
            fragmentNotificationsBinding.specificTimeLayout.setEnabled(true);
            fragmentNotificationsBinding.specificTime.setEnabled(true);
        }
    }

    public void createListenerSpecificTime() {
        fragmentNotificationsBinding.specificTime.setOnClickListener(view -> {
            MaterialTimePicker picker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(notificationsPreferences.getEventDailyTimeHour())
                            .setMinute(notificationsPreferences.getEventDailyTimeMinute())
                            .setTitleText(getContext().getString(R.string.fragment_notifications_time_dialog))
                            .build();

            picker.show(getParentFragmentManager(), picker.toString());

            picker.addOnPositiveButtonClickListener(v -> {
                notificationsPreferences.setEventDailyTimeHour(picker.getHour());
                notificationsPreferences.setEventDailyTimeMinute(picker.getMinute());

                fragmentNotificationsBinding.specificTime.setText(String.format(
                        Locale.getDefault(),
                        "%02d: %02d", picker.getHour(), picker.getMinute()
                ));
            });
        });
    }

    private void setSpecificTime() {
        final boolean booleanDaily = notificationsPreferences.getEventDaily();

        fragmentNotificationsBinding.checkBoxDailyAt.setChecked(booleanDaily);

        fragmentNotificationsBinding.specificTimeLayout.setEnabled(false);
        fragmentNotificationsBinding.specificTime.setEnabled(false);
        fragmentNotificationsBinding.specificTime.setFocusable(false);
        fragmentNotificationsBinding.specificTime.setClickable(true);

        if (booleanDaily) {
            fragmentNotificationsBinding.specificTimeLayout.setEnabled(true);
            fragmentNotificationsBinding.specificTime.setEnabled(true);
        }

        int hour = (notificationsPreferences.getEventDailyTimeHour() == -1) ? 6 : notificationsPreferences.getEventDailyTimeHour();
        int minute = (notificationsPreferences.getEventDailyTimeMinute() == -1) ? 0 : notificationsPreferences.getEventDailyTimeMinute();

        notificationsPreferences.setEventDailyTimeHour(hour);
        notificationsPreferences.setEventDailyTimeMinute(minute);

        String timeToDisplay = String.format(
                Locale.getDefault(),
                "%02d: %02d",
                hour,
                minute
        );
        fragmentNotificationsBinding.specificTime.setText(timeToDisplay);
    }

    private void setAction() {
        fragmentNotificationsBinding.radioButtonNextRandom.setChecked(notificationsPreferences.getEventNextRandom());
        fragmentNotificationsBinding.radioButtonNextSequential.setChecked(notificationsPreferences.getEventNextSequential());
    }

    private void setDisplay() {
        fragmentNotificationsBinding.radioButtonWhereInWidget.setChecked(notificationsPreferences.getEventDisplayWidget());
        fragmentNotificationsBinding.radioButtonWhereAsNotification.setChecked(notificationsPreferences.getEventDisplayWidgetAndNotification());

        if (fragmentNotificationsBinding.radioButtonWhereAsNotification.isChecked()) {
            fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);

            fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(true);
            fragmentNotificationsBinding.textViewNotificationSizeWarning1.setEnabled(true);
        } else {
            fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(false);

            fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(false);
            fragmentNotificationsBinding.textViewNotificationSizeWarning1.setEnabled(false);
        }

        fragmentNotificationsBinding.switchExcludeSourceFromNotification.setChecked(notificationsPreferences.getExcludeSourceFromNotification());
    }

    private void setDeviceUnlock() {
        fragmentNotificationsBinding.checkBoxDeviceUnlock.setChecked(notificationsPreferences.getEventDeviceUnlock());
    }

    private void createListenerNextRandom() {
        final RadioButton radioButtonNextRandom = fragmentNotificationsBinding.radioButtonNextRandom;
        radioButtonNextRandom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationsPreferences.getEventNextRandom() != isChecked) {
                notificationsPreferences.setEventNextRandom(isChecked);
            }
        });
    }

    private void createListenerNextSequential() {
        final RadioButton radioButtonNextSequential = fragmentNotificationsBinding.radioButtonNextSequential;
        radioButtonNextSequential.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationsPreferences.getEventNextSequential() != isChecked) {
                notificationsPreferences.setEventNextSequential(isChecked);
            }
        });
    }

    private void createListenerDisplayRadioGroup() {
        final RadioGroup radioGroup = fragmentNotificationsBinding.radioButtonDisplayGroup;
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonWhereInWidget:
                    notificationsPreferences.setEventDisplayWidget(true);
                    notificationsPreferences.setEventDisplayWidgetAndNotification(false);

                    fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(false);
                    fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(false);
                    fragmentNotificationsBinding.textViewNotificationSizeWarning1.setEnabled(false);
                    break;

                case R.id.radioButtonWhereAsNotification:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                getContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED) {

                            ConfigureActivity.launcherInvoked = true;

                            requestPermissionLauncherNotifications.launch(Manifest.permission.POST_NOTIFICATIONS);

                            break;
                        }
                    }
                    notificationsPreferences.setEventDisplayWidget(false);
                    notificationsPreferences.setEventDisplayWidgetAndNotification(true);
                    fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);
                    fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(true);
                    fragmentNotificationsBinding.textViewNotificationSizeWarning1.setEnabled(true);
                    break;
            }
        });
    }

    private void createListenerExcludeSourceFromNotification() {
        fragmentNotificationsBinding.switchExcludeSourceFromNotification.setOnCheckedChangeListener((buttonView, isChecked) ->
                notificationsPreferences.setExcludeSourceFromNotification(isChecked)
        );
    }

    private void createListenerDeviceUnlock() {
        final CheckBox checkBoxDeviceUnlock = fragmentNotificationsBinding.checkBoxDeviceUnlock;
        checkBoxDeviceUnlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationsPreferences.getEventDeviceUnlock() != isChecked) {
                notificationsPreferences.setEventDeviceUnlock(isChecked);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        handleSpecialPermissionForExactAlarm();
    }

    private void createListenerDaily() {
        final CheckBox checkBoxDailyAt = fragmentNotificationsBinding.checkBoxDailyAt;
        checkBoxDailyAt.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    ConfigureActivity.launcherInvoked = true;
                    startActivity(new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                } else {
                    createListenerDailyCommon(isChecked);
                }

            } else {
                createListenerDailyCommon(isChecked);
            }

        });
    }

    private void createListenerDailyCommon(boolean isChecked) {
        if (notificationsPreferences.getEventDaily() != isChecked) {
            notificationsPreferences.setEventDaily(isChecked);
        }

        fragmentNotificationsBinding.specificTimeLayout.setEnabled(false);
        fragmentNotificationsBinding.specificTime.setEnabled(false);
        fragmentNotificationsBinding.specificTime.setFocusable(false);

        if (isChecked) {
            fragmentNotificationsBinding.specificTimeLayout.setEnabled(true);
            fragmentNotificationsBinding.specificTime.setEnabled(true);
            fragmentNotificationsBinding.specificTime.setFocusable(true);
        }
    }

    private void createListenerCustomisableInterval() {
        final CheckBox checkBoxCustomisableInterval = fragmentNotificationsBinding.checkBoxCustomisableInterval;
        checkBoxCustomisableInterval.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    ConfigureActivity.launcherInvoked = true;
                    Intent intent = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    requestExactAlarmLauncher.launch(intent);
                } else {
                    notificationsPreferences.setCustomisableInterval(isChecked);
                    setCustomisableInterval();
                }
            } else {
                notificationsPreferences.setCustomisableInterval(isChecked);
                setCustomisableInterval();
            }

            Timber.d("%b", isChecked);
        });
    }

    private void setCustomisableInterval() {
        Boolean enabled = notificationsPreferences.getCustomisableInterval();

        fragmentNotificationsBinding.checkBoxCustomisableInterval.setChecked(enabled);

        fragmentNotificationsBinding.textViewActivetime.setEnabled(enabled);
        fragmentNotificationsBinding.sliderActiveHours.setEnabled(enabled);
        fragmentNotificationsBinding.sliderActiveHours.setEnabled(enabled);
        fragmentNotificationsBinding.sliderActiveHours.setClickable(enabled);
        fragmentNotificationsBinding.sliderActiveHours.setFocusable(enabled);

        fragmentNotificationsBinding.textViewCustomisableIntervalEvery.setEnabled(enabled);
        fragmentNotificationsBinding.spinnerCustomisableIntervalHour.setEnabled(enabled);
        fragmentNotificationsBinding.textViewCustomisableIntervalEveryHours.setEnabled(enabled);

        notificationsPreferences.setCustomisableInterval(enabled);
    }

    private void setCustomisableIntervalSliderHours() {
        Integer from = notificationsPreferences.getCustomisableIntervalHourFrom();
        Integer to = notificationsPreferences.getCustomisableIntervalHourTo();

        int[] hoursArray = getResources().getIntArray(R.array.fragment_notifications_quiet_time_array);
        if (from == -1) {
            from = hoursArray[0];
        }
        if (to == -1) {
            to = hoursArray[1];
        }

        fragmentNotificationsBinding.sliderActiveHours.setValues(from.floatValue(), to.floatValue());

        notificationsPreferences.setCustomisableIntervalHourFrom(from);
        notificationsPreferences.setCustomisableIntervalHourTo(to);
    }

    private void createListenerCustomisableIntervalSliderHours() {
        fragmentNotificationsBinding.sliderActiveHours.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            int from = values.get(0).intValue();
            int to = values.get(1).intValue();

            Timber.d("%d", from);
            notificationsPreferences.setCustomisableIntervalHourFrom(from);

            Timber.d("%d", to);
            notificationsPreferences.setCustomisableIntervalHourTo(to);

            setCustomisableIntervalSpinnerHours(
                    notificationsPreferences.getCustomisableIntervalHourTo() - notificationsPreferences.getCustomisableIntervalHourFrom()
            );
        });
    }

    private void createListenerCustomisableIntervalSpinnerHours() {
        fragmentNotificationsBinding.spinnerCustomisableIntervalHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int hourPosition = fragmentNotificationsBinding.spinnerCustomisableIntervalHour.getSelectedItemPosition();
                int[] hourArray = getResources().getIntArray(R.array.fragment_notifications_customisable_hour);

                Timber.d("spinner.selection=%d", hourArray[hourPosition]);
                notificationsPreferences.setCustomisableIntervalHours(hourArray[hourPosition]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setCustomisableIntervalSpinnerHours(int sliderHours) {
        Timber.d("spinner.sliderHours=0..%d", sliderHours);

        int[] sizeArray = getResources().getIntArray(R.array.fragment_notifications_customisable_hour);
        List<String> spinnerArray = new ArrayList<>();

        if (sliderHours >= 0) {
            spinnerArray.add("" + sizeArray[0]);
        }

        if (sliderHours >= 1) {
            spinnerArray.add("" + sizeArray[1]);
        }

        if (sliderHours >= 2) {
            spinnerArray.add("" + sizeArray[2]);
        }

        if (sliderHours >= 3) {
            spinnerArray.add("" + sizeArray[3]);
        }

        Timber.d("spinner.spinnerArray=%s", spinnerArray.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fragmentNotificationsBinding.spinnerCustomisableIntervalHour.setAdapter(adapter);

        if (sliderHours < notificationsPreferences.getCustomisableIntervalHours()) {
            fragmentNotificationsBinding.spinnerCustomisableIntervalHour.setSelection(0);
            notificationsPreferences.setCustomisableIntervalHours(1);
        } else {
            int savedHours = notificationsPreferences.getCustomisableIntervalHours();
            fragmentNotificationsBinding.spinnerCustomisableIntervalHour.setSelection(
                    savedHours - 1
            );
        }
    }
}
