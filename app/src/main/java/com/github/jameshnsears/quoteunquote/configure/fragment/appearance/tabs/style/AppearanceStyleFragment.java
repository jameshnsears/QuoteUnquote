package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabStyleBinding;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@Keep
public class AppearanceStyleFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceTabStyleBinding fragmentAppearanceTabStyleBinding;
    @Nullable
    public AppearancePreferences appearancePreferences;

    public AppearanceStyleFragment() {
        // dark mode support
    }

    public AppearanceStyleFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceStyleFragment newInstance(final int widgetId) {
        final AppearanceStyleFragment fragment = new AppearanceStyleFragment(widgetId);
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

        fragmentAppearanceTabStyleBinding = FragmentAppearanceTabStyleBinding.inflate(inflater.cloneInContext(context));
        return fragmentAppearanceTabStyleBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceTabStyleBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final @NonNull Bundle savedInstanceState) {
        createListenerTransparency();
        createListenerBackgroundColour();

        createListenerTextFamily();
        createListenerTextStyle();
        createListenerTextSize();
        createListenerTextColour();

        setTransparency();
        setBackgroundColour();

        setTextFamily();
        setTextStyle();
        setTextSize();
        setTextColour();
    }

    private void createListenerTransparency() {
        fragmentAppearanceTabStyleBinding.seekBarTransparency.addOnSliderTouchListener(
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
        final Spinner spinner = fragmentAppearanceTabStyleBinding.spinnerFamily;
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
        final Spinner spinner = fragmentAppearanceTabStyleBinding.spinnerStyle;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                appearancePreferences.setAppearanceTextStyle(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerBackgroundColour() {
        final Spinner spinner = fragmentAppearanceTabStyleBinding.spinnerColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                appearancePreferences.setAppearanceColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextSize() {
        final Spinner spinner = fragmentAppearanceTabStyleBinding.spinnerSize;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                appearancePreferences.setAppearanceTextSize(Integer.parseInt(spinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextColour() {
        final Spinner spinner = fragmentAppearanceTabStyleBinding.spinnerTextColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                appearancePreferences.setAppearanceTextColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    public void setTextSize() {
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

        fragmentAppearanceTabStyleBinding.spinnerSize.setAdapter(spinnerArrayAdapter);
        setTextSizePreference(sizeIntegerArray, fragmentAppearanceTabStyleBinding.spinnerSize);
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

    public void setTransparency() {
        final int transparency = appearancePreferences.getAppearanceTransparency();
        Timber.d("%d", transparency);

        if (transparency == -1) {
            fragmentAppearanceTabStyleBinding.seekBarTransparency.setValue(0);
        } else {
            fragmentAppearanceTabStyleBinding.seekBarTransparency.setValue(transparency);
        }
    }

    public void setTextFamily() {
        setSpinner(
                fragmentAppearanceTabStyleBinding.spinnerFamily,
                new AppearanceTextFamilySpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceTextFamily(),
                2,
                R.array.fragment_appearance_family_array
        );

        appearancePreferences.setAppearanceTextFamily(fragmentAppearanceTabStyleBinding.spinnerFamily.getSelectedItem().toString());
    }

    public void setTextStyle() {
        setSpinner(
                fragmentAppearanceTabStyleBinding.spinnerStyle,
                new AppearanceTextStyleSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceTextStyle(),
                3,
                R.array.fragment_appearance_style_array
        );

        appearancePreferences.setAppearanceTextStyle(fragmentAppearanceTabStyleBinding.spinnerStyle.getSelectedItem().toString());
    }

    public void setBackgroundColour() {
        setSpinner(
                this.fragmentAppearanceTabStyleBinding.spinnerColour,
                new AppearanceBackgroundColourSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceColour(),
                1,
                R.array.fragment_appearance_colour_array
        );

        appearancePreferences.setAppearanceColour(fragmentAppearanceTabStyleBinding.spinnerColour.getSelectedItem().toString());
    }

    public void setTextColour() {
        setSpinner(
                fragmentAppearanceTabStyleBinding.spinnerTextColour,
                new AppearanceTextColourSpinnerAdapter(getActivity().getBaseContext()),
                appearancePreferences.getAppearanceTextColour(),
                14,
                R.array.fragment_appearance_text_colour_array
        );

        appearancePreferences.setAppearanceTextColour(fragmentAppearanceTabStyleBinding.spinnerTextColour.getSelectedItem().toString());
    }
}
