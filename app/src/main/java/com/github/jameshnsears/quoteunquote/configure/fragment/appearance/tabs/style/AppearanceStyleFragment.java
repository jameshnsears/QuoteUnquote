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

    public AppearanceStyleFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceStyleFragment newInstance(int widgetId) {
        AppearanceStyleFragment fragment = new AppearanceStyleFragment(widgetId);
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

        this.fragmentAppearanceTabStyleBinding = FragmentAppearanceTabStyleBinding.inflate(inflater.cloneInContext(context));
        return this.fragmentAppearanceTabStyleBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentAppearanceTabStyleBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull View view, Bundle savedInstanceState) {
        this.createListenerTransparency();
        this.createListenerBackgroundColour();

        this.createListenerTextFamily();
        this.createListenerTextStyle();
        this.createListenerTextSize();
        this.createListenerTextColour();

        this.setTransparency();
        this.setBackgroundColour();

        this.setTextFamily();
        this.setTextStyle();
        this.setTextSize();
        this.setTextColour();
    }

    private void createListenerTransparency() {
        this.fragmentAppearanceTabStyleBinding.seekBarTransparency.addOnSliderTouchListener(
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull final Slider slider) {
                        // ...
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull final Slider slider) {
                        final int sliderValue = (int) slider.getValue();
                        Timber.d("%d", sliderValue);
                        AppearanceStyleFragment.this.appearancePreferences.setAppearanceTransparency(sliderValue);
                    }
                });
    }

    private void createListenerTextFamily() {
        Spinner spinner = this.fragmentAppearanceTabStyleBinding.spinnerFamily;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                final String selectedItem = spinner.getSelectedItem().toString();
                if (!AppearanceStyleFragment.this.appearancePreferences.getAppearanceTextFamily().equals(selectedItem)) {
                    AppearanceStyleFragment.this.appearancePreferences.setAppearanceTextFamily(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerTextStyle() {
        Spinner spinner = this.fragmentAppearanceTabStyleBinding.spinnerStyle;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                AppearanceStyleFragment.this.appearancePreferences.setAppearanceTextStyle(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerBackgroundColour() {
        Spinner spinner = this.fragmentAppearanceTabStyleBinding.spinnerColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                AppearanceStyleFragment.this.appearancePreferences.setAppearanceColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextSize() {
        Spinner spinner = this.fragmentAppearanceTabStyleBinding.spinnerSize;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                AppearanceStyleFragment.this.appearancePreferences.setAppearanceTextSize(Integer.parseInt(spinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextColour() {
        Spinner spinner = this.fragmentAppearanceTabStyleBinding.spinnerTextColour;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                AppearanceStyleFragment.this.appearancePreferences.setAppearanceTextColour(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    public void setTextSize() {
        int[] sizeArray = this.getResources().getIntArray(R.array.fragment_appearance_size_array);
        List<Integer> sizeIntegerArray = this.getTextSizeIntegerArray(sizeArray);

        ArrayAdapter<Integer> spinnerArrayAdapter =
                new ArrayAdapter<Integer>(
                        this.getContext(),
                        android.R.layout.simple_list_item_1,
                        sizeIntegerArray) {

                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        return AppearanceStyleFragment.this.getTextSizeDefault(
                                position,
                                super.getView(position, convertView, parent),
                                sizeIntegerArray);
                    }

                    @NonNull
                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        return AppearanceStyleFragment.this.getTextSizeDefault(
                                position,
                                super.getDropDownView(position, convertView, parent),
                                sizeIntegerArray);
                    }
                };

        this.fragmentAppearanceTabStyleBinding.spinnerSize.setAdapter(spinnerArrayAdapter);
        this.setTextSizePreference(sizeIntegerArray, this.fragmentAppearanceTabStyleBinding.spinnerSize);
    }

    private void setTextSizePreference(
            @NonNull List<Integer> sizeIntegerArray,
            @NonNull Spinner spinnerSize) {
        int testSizePreference = appearancePreferences.getAppearanceTextSize();
        if (testSizePreference == -1) {
            if (this.getContext().getResources().getConfiguration().smallestScreenWidthDp >= 600) {
                // tablet
                spinnerSize.setSelection(4);
            } else {
                // phone
                spinnerSize.setSelection(2);
            }
        } else {
            int selectionIndex = 0;
            for (Integer sizeInteger : sizeIntegerArray) {
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
            int position,
            @NonNull View view,
            @NonNull List<Integer> sizeIntegerArray) {
        TextView textView = (TextView) view;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeIntegerArray.get(position).floatValue());
        return view;
    }

    @NonNull
    private List<Integer> getTextSizeIntegerArray(@NonNull int... sizeArray) {
        List<Integer> sizeIntegerArray = new ArrayList<>();
        for (int size : sizeArray) {
            Integer integer = size;
            sizeIntegerArray.add(integer);
        }
        return sizeIntegerArray;
    }

    public void setTransparency() {
        int transparency = this.appearancePreferences.getAppearanceTransparency();
        Timber.d("%d", transparency);

        if (transparency == -1) {
            this.fragmentAppearanceTabStyleBinding.seekBarTransparency.setValue(0);
        } else {
            this.fragmentAppearanceTabStyleBinding.seekBarTransparency.setValue(transparency);
        }
    }

    public void setTextFamily() {
        this.setSpinner(
                this.fragmentAppearanceTabStyleBinding.spinnerFamily,
                new AppearanceTextFamilySpinnerAdapter(this.getActivity().getBaseContext()),
                this.appearancePreferences.getAppearanceTextFamily(),
                2,
                R.array.fragment_appearance_family_array
        );

        this.appearancePreferences.setAppearanceTextFamily(this.fragmentAppearanceTabStyleBinding.spinnerFamily.getSelectedItem().toString());
    }

    public void setTextStyle() {
        this.setSpinner(
                this.fragmentAppearanceTabStyleBinding.spinnerStyle,
                new AppearanceTextStyleSpinnerAdapter(this.getActivity().getBaseContext()),
                this.appearancePreferences.getAppearanceTextStyle(),
                3,
                R.array.fragment_appearance_style_array
        );

        this.appearancePreferences.setAppearanceTextStyle(this.fragmentAppearanceTabStyleBinding.spinnerStyle.getSelectedItem().toString());
    }

    public void setBackgroundColour() {
        this.setSpinner(
                fragmentAppearanceTabStyleBinding.spinnerColour,
                new AppearanceBackgroundColourSpinnerAdapter(this.getActivity().getBaseContext()),
                this.appearancePreferences.getAppearanceColour(),
                1,
                R.array.fragment_appearance_colour_array
        );

        this.appearancePreferences.setAppearanceColour(this.fragmentAppearanceTabStyleBinding.spinnerColour.getSelectedItem().toString());
    }

    public void setTextColour() {
        this.setSpinner(
                this.fragmentAppearanceTabStyleBinding.spinnerTextColour,
                new AppearanceTextColourSpinnerAdapter(this.getActivity().getBaseContext()),
                this.appearancePreferences.getAppearanceTextColour(),
                14,
                R.array.fragment_appearance_text_colour_array
        );

        this.appearancePreferences.setAppearanceTextColour(this.fragmentAppearanceTabStyleBinding.spinnerTextColour.getSelectedItem().toString());
    }
}
