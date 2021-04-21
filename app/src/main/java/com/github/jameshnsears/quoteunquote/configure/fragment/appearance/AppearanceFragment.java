package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceBinding;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AppearanceFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceBinding fragmentAppearanceBinding;
    @Nullable
    public AppearancePreferences appearancePreferences;

    protected AppearanceFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceFragment newInstance(final int widgetId) {
        final AppearanceFragment fragment = new AppearanceFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final Context context = new ContextThemeWrapper(getActivity(), R.style.AppTheme);

        appearancePreferences = new AppearancePreferences(this.widgetId, getContext());

        fragmentAppearanceBinding = FragmentAppearanceBinding.inflate(inflater.cloneInContext(context));
        return fragmentAppearanceBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final Bundle savedInstanceState) {
        createListenerTransparency();
        createListenerColour();

        createListenerTextFamily();
        createListenerTextStyle();
        createListenerTextSize();

        createListenerToolbarFirst();
        createListenerToolbarPrevious();
        createListenerToolbarReport();
        createListenerToolbarToggleFavourite();
        createListenerToolbarShare();
        createListenerToolbarNextRandom();
        createListenerToolbarNextSequential();

        setTransparency();
        setColour();

        setTextFamily();
        setTextStyle();
        setTextSize();

        setToolbar();
    }

    private void setToolbar() {
        fragmentAppearanceBinding.toolbarSwitchFirst.setChecked(appearancePreferences.getAppearanceToolbarFirst());
        fragmentAppearanceBinding.toolbarSwitchPrevious.setChecked(appearancePreferences.getAppearanceToolbarPrevious());
        fragmentAppearanceBinding.toolbarSwitchReport.setChecked(appearancePreferences.getAppearanceToolbarReport());
        fragmentAppearanceBinding.toolbarSwitchToggleFavourite.setChecked(appearancePreferences.getAppearanceToolbarFavourite());
        fragmentAppearanceBinding.toolbarSwitchShare.setChecked(appearancePreferences.getAppearanceToolbarShare());
        fragmentAppearanceBinding.toolbarSwitchNextRandom.setChecked(appearancePreferences.getAppearanceToolbarRandom());
        fragmentAppearanceBinding.toolbarSwitchNextSequential.setChecked(appearancePreferences.getAppearanceToolbarSequential());
    }

    private void createListenerToolbarFirst() {
        fragmentAppearanceBinding.toolbarSwitchFirst.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarFirst(isChecked)
        );
    }

    private void createListenerToolbarPrevious() {
        fragmentAppearanceBinding.toolbarSwitchPrevious.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarPrevious(isChecked)
        );
    }

    private void createListenerToolbarReport() {
        fragmentAppearanceBinding.toolbarSwitchReport.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarReport(isChecked)
        );
    }

    private void createListenerToolbarToggleFavourite() {
        fragmentAppearanceBinding.toolbarSwitchToggleFavourite.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarFavourite(isChecked)
        );
    }

    private void createListenerToolbarShare() {
        fragmentAppearanceBinding.toolbarSwitchShare.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarShare(isChecked)
        );
    }

    private void createListenerToolbarNextRandom() {
        fragmentAppearanceBinding.toolbarSwitchNextRandom.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarRandom(isChecked)
        );
    }

    private void createListenerToolbarNextSequential() {
        fragmentAppearanceBinding.toolbarSwitchNextSequential.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearancePreferences.setAppearanceToolbarSequential(isChecked)
        );
    }

    private void createListenerTransparency() {
        fragmentAppearanceBinding.seekBarTransparency.addOnSliderTouchListener(
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        // ...
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        int sliderValue = (int) slider.getValue();
                        Timber.d("%d", sliderValue);
                        appearancePreferences.setAppearanceTransparency(sliderValue);
                    }
                });
    }

    private void createListenerTextFamily() {
        final Spinner spinner = fragmentAppearanceBinding.spinnerFamily;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = spinner.getSelectedItem().toString();
                if (!appearancePreferences.getAppearanceTextFamily().equals(selectedItem)) {
                    appearancePreferences.setAppearanceTextFamily(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerTextStyle() {
        final Spinner spinner = fragmentAppearanceBinding.spinnerStyle;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = spinner.getSelectedItem().toString();
                if (!appearancePreferences.getAppearanceTextStyle().equals(selectedItem)) {
                    appearancePreferences.setAppearanceTextStyle(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerColour() {
        final Spinner spinner = fragmentAppearanceBinding.spinnerColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                String selectedItem = spinner.getSelectedItem().toString();
                if (!appearancePreferences.getAppearanceColour().equals(selectedItem)) {
                    appearancePreferences.setAppearanceColour(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextSize() {
        final Spinner spinner = fragmentAppearanceBinding.spinnerSize;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                int selectedItem = Integer.parseInt(spinner.getSelectedItem().toString());
                if (appearancePreferences.getAppearanceTextSize() != selectedItem) {
                    appearancePreferences.setAppearanceTextSize(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void setTextSize() {
        final int[] sizeArray = getResources().getIntArray(R.array.fragment_appearance_size_array);
        final List<Integer> sizeIntegerArray = getTextSizeIntegerArray(sizeArray);

        final ArrayAdapter<Integer> spinnerArrayAdapter =
                new ArrayAdapter<Integer>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        sizeIntegerArray) {

                    @NonNull
                    @Override
                    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
                        return getTextSizeDefault(
                                position,
                                super.getView(position, convertView, parent),
                                sizeIntegerArray);
                    }

                    @NonNull
                    @Override
                    public View getDropDownView(final int position, final View convertView, @NonNull final ViewGroup parent) {
                        return getTextSizeDefault(
                                position,
                                super.getDropDownView(position, convertView, parent),
                                sizeIntegerArray);
                    }
                };

        fragmentAppearanceBinding.spinnerSize.setAdapter(spinnerArrayAdapter);
        setTextSizePreference(sizeIntegerArray, fragmentAppearanceBinding.spinnerSize);
    }

    private void setTextSizePreference(
            @NonNull final List<Integer> sizeIntegerArray,
            @NonNull final Spinner spinnerSize) {
        final int testSizePreference = this.appearancePreferences.getAppearanceTextSize();
        if (testSizePreference == -1) {
            if (getContext().getResources().getConfiguration().smallestScreenWidthDp >= 600) {
                // tablet
                spinnerSize.setSelection(4);
            } else {
                // phone
                spinnerSize.setSelection(2);
            }
        } else {
            int selectionIndex = 0;
            for (final Integer sizeInteger : sizeIntegerArray) {
                if (testSizePreference == sizeInteger) {
                    spinnerSize.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }

    @NonNull
    public View getTextSizeDefault(
            final int position,
            @NonNull final View view,
            @NonNull final List<Integer> sizeIntegerArray) {
        final TextView textView = (TextView) view;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeIntegerArray.get(position).floatValue());
        return view;
    }

    @NonNull
    private List<Integer> getTextSizeIntegerArray(@NonNull final int... sizeArray) {
        final List<Integer> sizeIntegerArray = new ArrayList<>();
        for (final int size : sizeArray) {
            final Integer integer = size;
            sizeIntegerArray.add(integer);
        }
        return sizeIntegerArray;
    }

    protected void setTransparency() {
        final int transparency = appearancePreferences.getAppearanceTransparency();
        Timber.d("%d", transparency);

        if (transparency == -1) {
            fragmentAppearanceBinding.seekBarTransparency.setValue(0);
        } else {
            fragmentAppearanceBinding.seekBarTransparency.setValue(transparency);
        }
    }

    protected void setTextFamily() {
        setSpinner(
                fragmentAppearanceBinding.spinnerFamily,
                new AppearanceFamilySpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceTextFamily(),
                2,
                R.array.fragment_appearance_family_array
        );

        appearancePreferences.setAppearanceTextFamily(fragmentAppearanceBinding.spinnerFamily.getSelectedItem().toString());
    }

    protected void setTextStyle() {
        setSpinner(
                fragmentAppearanceBinding.spinnerStyle,
                new AppearanceStyleSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceTextStyle(),
                3,
                R.array.fragment_appearance_style_array
        );

        appearancePreferences.setAppearanceTextStyle(fragmentAppearanceBinding.spinnerStyle.getSelectedItem().toString());
    }

    protected void setColour() {
        setSpinner(
                fragmentAppearanceBinding.spinnerColour,
                new AppearanceColourSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceColour(),
                1,
                R.array.fragment_appearance_colour_array
        );
    }

    private void setSpinner(
            @NonNull
            final Spinner spinner,
            @NonNull
            final BaseAdapter spinnerAdapter,
            @NonNull
            final String preference,
            int defaultSelection,
            int resourceArrayId) {

        spinner.setAdapter(spinnerAdapter);

        if ("".equals(preference)) {
            spinner.setSelection(defaultSelection);
        } else {
            int selectionIndex = 0;
            for (final String spinnerRow : getActivity().getBaseContext().getResources().getStringArray(resourceArrayId)) {
                if (spinnerRow.equals(preference)) {
                    spinner.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }
}
