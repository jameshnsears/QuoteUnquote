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

import java.util.Locale;

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
                    fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(true);
                }

                ConfigureActivity.launcherInvoked = false;
            });

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

        setBihourly();

        setSpecificTime();

        createListenerNextRandom();
        createListenerNextSequential();

        createListenerDisplayRadioGroup();
        createListenerExcludeSourceFromNotification();

        createListenerDeviceUnlock();

        createListenerBihourly();

        createListenerSpecificTime();
        createListenerDaily();
    }

    private void handleSpecialPermissionForExactAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            fragmentNotificationsBinding.textViewExactTimeWarningInfo.setVisibility(VISIBLE);
            fragmentNotificationsBinding.textViewExactTimeWarning.setVisibility(VISIBLE);

            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                fragmentNotificationsBinding.checkBoxBihourly.setChecked(false);
                notificationsPreferences.setEventBihourly(false);

                fragmentNotificationsBinding.checkBoxDailyAt.setChecked(false);
                notificationsPreferences.setEventDaily(false);
            } else {
                handleSpecialPermissionForExactAlarmCommon();
            }
        } else {
            handleSpecialPermissionForExactAlarmCommon();

            fragmentNotificationsBinding.textViewExactTimeWarningInfo.setVisibility(View.GONE);
            fragmentNotificationsBinding.textViewExactTimeWarning.setVisibility(View.GONE);
        }
    }

    private void handleSpecialPermissionForExactAlarmCommon() {
        if (fragmentNotificationsBinding.checkBoxBihourly.isChecked()) {
            notificationsPreferences.setEventBihourly(true);
        }

        if (fragmentNotificationsBinding.checkBoxDailyAt.isChecked()) {
            notificationsPreferences.setEventDaily(true);
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
            fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(true);
        } else {
            fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(false);

            fragmentNotificationsBinding.textViewNotificationSizeWarningInfo.setEnabled(false);
            fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(false);
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
                    fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(false);
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
                    fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(true);
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

    private void createListenerBihourly() {
        final CheckBox checkBoxBihourly = fragmentNotificationsBinding.checkBoxBihourly;
        checkBoxBihourly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    ConfigureActivity.launcherInvoked = true;
                    startActivity(new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                } else {
                    createListenerBihourlyCommon(isChecked);
                }
            } else {
                createListenerBihourlyCommon(isChecked);
            }
        });
    }

    private void createListenerBihourlyCommon(boolean isChecked) {
        if (notificationsPreferences.getEventBihourly() != isChecked) {
            notificationsPreferences.setEventBihourly(isChecked);
        }
    }

    private void setBihourly() {
        fragmentNotificationsBinding.checkBoxBihourly.setChecked(notificationsPreferences.getEventBihourly());
    }
}
