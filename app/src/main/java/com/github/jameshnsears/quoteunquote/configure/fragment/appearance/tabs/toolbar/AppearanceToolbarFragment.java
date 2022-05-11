package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabToolbarBinding;

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
        createListenerToolbarColour();
        createListenerToolbarFirst();
        createListenerToolbarPrevious();
        createListenerToolbarToggleFavourite();
        createListenerToolbarShare();
        createListenerToolbarNextRandom();
        createListenerToolbarNextSequential();

        setToolbarColour();
        setToolbar();
    }

    public void setToolbar() {
        fragmentAppearanceTabToolbarBinding.toolbarSwitchFirst.setChecked(appearancePreferences.getAppearanceToolbarFirst());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchPrevious.setChecked(appearancePreferences.getAppearanceToolbarPrevious());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchToggleFavourite.setChecked(appearancePreferences.getAppearanceToolbarFavourite());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchShare.setChecked(appearancePreferences.getAppearanceToolbarShare());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextRandom.setChecked(appearancePreferences.getAppearanceToolbarRandom());
        fragmentAppearanceTabToolbarBinding.toolbarSwitchNextSequential.setChecked(appearancePreferences.getAppearanceToolbarSequential());
    }

    private void createListenerToolbarColour() {
        final Spinner spinner = fragmentAppearanceTabToolbarBinding.spinnerToolbarColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                appearancePreferences.setAppearanceToolbarColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
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
        setSpinner(
                fragmentAppearanceTabToolbarBinding.spinnerToolbarColour,
                new AppearanceToolbarColourSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceToolbarColour(),
                0,
                R.array.fragment_appearance_toolbar_colour_array
        );

        appearancePreferences.setAppearanceToolbarColour(fragmentAppearanceTabToolbarBinding.spinnerToolbarColour.getSelectedItem().toString());
    }
}
