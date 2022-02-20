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

    public AppearanceToolbarFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceToolbarFragment newInstance(int widgetId) {
        AppearanceToolbarFragment fragment = new AppearanceToolbarFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Context context = new ContextThemeWrapper(this.getActivity(), R.style.Theme_MaterialComponents_DayNight);

        this.appearancePreferences = new AppearancePreferences(widgetId, this.getContext());

        this.fragmentAppearanceTabToolbarBinding = FragmentAppearanceTabToolbarBinding.inflate(inflater.cloneInContext(context));
        return this.fragmentAppearanceTabToolbarBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentAppearanceTabToolbarBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {
        this.createListenerToolbarColour();
        this.createListenerToolbarFirst();
        this.createListenerToolbarPrevious();
        this.createListenerToolbarToggleFavourite();
        this.createListenerToolbarShare();
        this.createListenerToolbarNextRandom();
        this.createListenerToolbarNextSequential();

        this.setToolbarColour();
        this.setToolbar();
    }

    public void setToolbar() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchFirst.setChecked(this.appearancePreferences.getAppearanceToolbarFirst());
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchPrevious.setChecked(this.appearancePreferences.getAppearanceToolbarPrevious());
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchToggleFavourite.setChecked(this.appearancePreferences.getAppearanceToolbarFavourite());
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchShare.setChecked(this.appearancePreferences.getAppearanceToolbarShare());
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchNextRandom.setChecked(this.appearancePreferences.getAppearanceToolbarRandom());
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchNextSequential.setChecked(this.appearancePreferences.getAppearanceToolbarSequential());
    }

    private void createListenerToolbarColour() {
        Spinner spinner = this.fragmentAppearanceTabToolbarBinding.spinnerToolbarColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                AppearanceToolbarFragment.this.appearancePreferences.setAppearanceToolbarColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerToolbarFirst() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchFirst.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarFirst(isChecked)
        );
    }

    private void createListenerToolbarPrevious() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchPrevious.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarPrevious(isChecked)
        );
    }

    private void createListenerToolbarToggleFavourite() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchToggleFavourite.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarFavourite(isChecked)
        );
    }

    private void createListenerToolbarShare() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchShare.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarShare(isChecked)
        );
    }

    private void createListenerToolbarNextRandom() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchNextRandom.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarRandom(isChecked)
        );
    }

    private void createListenerToolbarNextSequential() {
        this.fragmentAppearanceTabToolbarBinding.toolbarSwitchNextSequential.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.appearancePreferences.setAppearanceToolbarSequential(isChecked)
        );
    }

    public void setToolbarColour() {
        this.setSpinner(
                this.fragmentAppearanceTabToolbarBinding.spinnerToolbarColour,
                new AppearanceToolbarColourSpinnerAdapter(this.getActivity().getBaseContext()),
                this.appearancePreferences.getAppearanceToolbarColour(),
                0,
                R.array.fragment_appearance_toolbar_colour_array
        );

        this.appearancePreferences.setAppearanceToolbarColour(this.fragmentAppearanceTabToolbarBinding.spinnerToolbarColour.getSelectedItem().toString());
    }
}
