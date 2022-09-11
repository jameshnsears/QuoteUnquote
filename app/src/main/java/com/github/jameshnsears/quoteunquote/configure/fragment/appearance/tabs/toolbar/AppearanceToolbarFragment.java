package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabToolbarBinding;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

@Keep
public class AppearanceToolbarFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceTabToolbarBinding fragmentAppearanceTabToolbarBinding;
    @Nullable
    public AppearancePreferences appearancePreferences;

    public AppearanceToolbarFragment() {
        // dark mode support
    }

    public AppearanceToolbarFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceToolbarFragment newInstance(final int widgetId) {
        final AppearanceToolbarFragment fragment = new AppearanceToolbarFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final @NonNull ViewGroup container,
            final @NonNull Bundle savedInstanceState) {
        final Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_MaterialComponents_DayNight);

        appearancePreferences = new AppearancePreferences(this.widgetId, getContext());

        fragmentAppearanceTabToolbarBinding = FragmentAppearanceTabToolbarBinding.inflate(inflater.cloneInContext(context));
        return fragmentAppearanceTabToolbarBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceTabToolbarBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        createListenerHideSeparator();

        createListenerToolbarColourPicker();
        createListenerToolbarFirst();
        createListenerToolbarPrevious();
        createListenerToolbarToggleFavourite();
        createListenerToolbarShare();
        createListenerToolbarJump();
        createListenerToolbarNextRandom();
        createListenerToolbarNextSequential();

        setHideSeparator();
        setToolbarColour();
        setToolbar();
    }

    public void setToolbar() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchFirst.setChecked(appearancePreferences.getAppearanceToolbarFirst());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchPrevious.setChecked(appearancePreferences.getAppearanceToolbarPrevious());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchToggleFavourite.setChecked(appearancePreferences.getAppearanceToolbarFavourite());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchShare.setChecked(appearancePreferences.getAppearanceToolbarShare());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchJump.setChecked(appearancePreferences.getAppearanceToolbarJump());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextRandom.setChecked(appearancePreferences.getAppearanceToolbarRandom());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextSequential.setChecked(appearancePreferences.getAppearanceToolbarSequential());
    }

    private void createListenerToolbarColourPicker() {
        fragmentAppearanceTabToolbarBinding.toolbarColourPickerButton.setOnClickListener(v -> {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext())
                    .setTitle(getString(R.string.fragment_appearance_toolbar_colour_dialog_title))
                    .setPositiveButton(getString(R.string.fragment_appearance_ok),
                            (ColorEnvelopeListener) (envelope, fromUser) -> {

                                fragmentAppearanceTabToolbarBinding
                                        .toolbarColourPickerButton
                                        .setBackgroundColor(envelope.getColor());

                                appearancePreferences.setAppearanceToolbarColour("#" + envelope.getHexCode());
                            }

                    )
                    .setNegativeButton(getString(R.string.fragment_appearance_cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true);

            ColorPickerView colorPickerView = builder.getColorPickerView();

            String appearanceToolbarColour = appearancePreferences.getAppearanceToolbarColour();
            appearanceToolbarColour = appearanceToolbarColour.replace("#", "");
            int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceToolbarColour, 16);
            colorPickerView.setInitialColor(appearanceColourUnsignedInt);

            colorPickerView.getBrightnessSlider().invalidate();
            builder.show();
        });
    }

    public void setHideSeparator() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchHideSeparator.setChecked(appearancePreferences.getAppearanceToolbarHideSeparator());
    }

    private void createListenerHideSeparator() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchHideSeparator.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarHideSeparator(isChecked)
        );
    }

    private void createListenerToolbarFirst() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchFirst.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarFirst(isChecked)
        );
    }

    private void createListenerToolbarPrevious() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchPrevious.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarPrevious(isChecked)
        );
    }

    private void createListenerToolbarToggleFavourite() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchToggleFavourite.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarFavourite(isChecked)
        );
    }

    private void createListenerToolbarShare() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchShare.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarShare(isChecked)
        );
    }

    private void createListenerToolbarJump() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchJump.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarJump(isChecked)
        );
    }

    private void createListenerToolbarNextRandom() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextRandom.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarRandom(isChecked)
        );
    }

    private void createListenerToolbarNextSequential() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextSequential.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarSequential(isChecked)
        );
    }

    public void setToolbarColour() {
        String appearanceToolbarColour = appearancePreferences.getAppearanceToolbarColour();
        appearanceToolbarColour = appearanceToolbarColour.replace("#", "");
        int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceToolbarColour, 16);
        fragmentAppearanceTabToolbarBinding
                .toolbarColourPickerButton.setBackgroundColor(appearanceColourUnsignedInt);
    }
}
