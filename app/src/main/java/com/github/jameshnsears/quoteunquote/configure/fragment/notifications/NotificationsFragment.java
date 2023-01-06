package com.github.jameshnsears.quoteunquote.configure.fragment.notifications;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentNotificationsBinding;

@Keep
public class NotificationsFragment extends FragmentCommon {
    @Nullable
    public FragmentNotificationsBinding fragmentNotificationsBinding;

    @Nullable
    public NotificationsPreferences notificationsPreferences;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    fragmentNotificationsBinding.radioButtonWhereInWidget.performClick();
                } else {
                    notificationsPreferences.setEventDisplayWidget(false);
                    notificationsPreferences.setEventDisplayWidgetAndNotification(true);
                    fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);
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
        setNext();
        setDisplay();
        setDeviceUnlock();
        setDaily();
        setDailyTime();
        setBihourly();

        createListenerNextRandom();
        createListenerNextSequential();

        createListenerDisplayRadioGroup();
        createListenerExcludeSourceFromNotification();

        createListenerBihourly();
        createListenerDeviceUnlock();
        createListenerDaily();
        createListenerDailyTime();
    }

    private void setDaily() {
        final boolean booleanDaily = notificationsPreferences.getEventDaily();

        fragmentNotificationsBinding.checkBoxDailyAt.setChecked(booleanDaily);

        final TimePicker timePicker = fragmentNotificationsBinding.timePickerDailyAt;

        timePicker.setEnabled(false);
        if (booleanDaily) {
            timePicker.setEnabled(true);
        }
    }

    private void setNext() {
        fragmentNotificationsBinding.radioButtonNextRandom.setChecked(notificationsPreferences.getEventNextRandom());
        fragmentNotificationsBinding.radioButtonNextSequential.setChecked(notificationsPreferences.getEventNextSequential());
    }

    private void setDisplay() {
        fragmentNotificationsBinding.radioButtonWhereInWidget.setChecked(notificationsPreferences.getEventDisplayWidget());
        fragmentNotificationsBinding.radioButtonWhereAsNotification.setChecked(notificationsPreferences.getEventDisplayWidgetAndNotification());

        if (fragmentNotificationsBinding.radioButtonWhereAsNotification.isChecked()) {
            fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);

            fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(true);
        } else {
            fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(false);

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
                    fragmentNotificationsBinding.textViewNotificationSizeWarning.setEnabled(false);
                    break;

                case R.id.radioButtonWhereAsNotification:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                getContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED) {

                            ConfigureActivity.launcherInvoked = true;

                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

                            break;
                        }
                    }
                    notificationsPreferences.setEventDisplayWidget(false);
                    notificationsPreferences.setEventDisplayWidgetAndNotification(true);
                    fragmentNotificationsBinding.switchExcludeSourceFromNotification.setEnabled(true);
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

    private void createListenerDaily() {
        final CheckBox checkBoxDailyAt = fragmentNotificationsBinding.checkBoxDailyAt;
        checkBoxDailyAt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationsPreferences.getEventDaily() != isChecked) {
                notificationsPreferences.setEventDaily(isChecked);
            }

            final TimePicker timePicker = fragmentNotificationsBinding.timePickerDailyAt;

            timePicker.setEnabled(false);
            if (isChecked) {
                timePicker.setEnabled(true);
            }
        });
    }

    private void createListenerDailyTime() {
        final TimePicker timePicker = fragmentNotificationsBinding.timePickerDailyAt;
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
                    int h = timePicker.getHour();
                    if (notificationsPreferences.getEventDailyTimeHour() != h) {
                        notificationsPreferences.setEventDailyTimeHour(h);
                    }

                    int m = timePicker.getMinute();
                    if (notificationsPreferences.getEventDailyTimeMinute() != m) {
                        notificationsPreferences.setEventDailyTimeMinute(m);
                    }
                }
        );
    }

    private void createListenerBihourly() {
        final CheckBox checkBoxBihourly = fragmentNotificationsBinding.checkBoxBihourly;
        checkBoxBihourly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (notificationsPreferences.getEventBihourly() != isChecked) {
                notificationsPreferences.setEventBihourly(isChecked);
            }
        });
    }

    protected void setDailyTime() {
        final TimePicker timePicker = fragmentNotificationsBinding.timePickerDailyAt;

        final int hourOfDay = notificationsPreferences.getEventDailyTimeHour();
        if (hourOfDay == -1) {
            notificationsPreferences.setEventDailyTimeHour(6);
            timePicker.setHour(6);
        } else {
            timePicker.setHour(hourOfDay);
        }

        final int minute = notificationsPreferences.getEventDailyTimeMinute();
        if (minute == -1) {
            notificationsPreferences.setEventDailyTimeMinute(0);
            timePicker.setMinute(0);
        } else {
            timePicker.setMinute(minute);
        }

        timePicker.setIs24HourView(false);
    }

    private void setBihourly() {
        fragmentNotificationsBinding.checkBoxBihourly.setChecked(notificationsPreferences.getEventBihourly());
    }
}
