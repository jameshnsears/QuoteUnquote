package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceBinding;
import com.github.jameshnsears.quoteunquote.utils.Preferences;

import java.util.ArrayList;
import java.util.List;


public final class FragmentAppearance extends FragmentCommon {
    private static final String LOG_TAG = FragmentAppearance.class.getSimpleName();

    public FragmentAppearanceBinding fragmentAppearanceBinding;

    private FragmentAppearance(final int widgetId) {
        super(LOG_TAG, widgetId);
    }

    public static FragmentAppearance newInstance(final int widgetId) {
        final FragmentAppearance fragment = new FragmentAppearance(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        fragmentAppearanceBinding = FragmentAppearanceBinding.inflate(getLayoutInflater());
        return fragmentAppearanceBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceBinding = null;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        setTransparency();
        setColour();
        setSize();
        setToolbarViaSharedPreference();

        createSeekBarListener();
        createSpinnerListener(view, fragmentAppearanceBinding.spinnerSize.getId());
        createSpinnerListener(view, fragmentAppearanceBinding.spinnerColour.getId());
        createCheckBoxToolbarListener();
    }

    private void setToolbarViaSharedPreference() {
        final boolean booleanEnableToolbar
                = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_APPEARANCE,
                Preferences.CHECK_BOX_TOOLBAR,
                true);

        preferences.setSharedPreference(
                Preferences.FRAGMENT_APPEARANCE,
                Preferences.CHECK_BOX_TOOLBAR,
                booleanEnableToolbar);

        fragmentAppearanceBinding.checkBoxDisplayToolbar.setChecked(booleanEnableToolbar);
    }

    private void createSeekBarListener() {
        fragmentAppearanceBinding.seekBarTransparency.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                        Log.d(LOG_TAG, "progress=" + progress);
                        preferences.setSharedPreference(
                                Preferences.FRAGMENT_APPEARANCE,
                                Preferences.SEEK_BAR,
                                progress);
                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {
                        // dp nothing
                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {
                        // do nothing
                    }
                });
    }

    private void createSpinnerListener(final View view, final int resourceId) {
        final Spinner spinner = view.findViewById(resourceId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (resourceId != fragmentAppearanceBinding.spinnerSize.getId()) {
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_APPEARANCE,
                            getResources().getResourceEntryName(resourceId),
                            spinner.getSelectedItem().toString());
                } else {
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_APPEARANCE,
                            getResources().getResourceEntryName(resourceId),
                            Integer.parseInt(spinner.getSelectedItem().toString()));
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createCheckBoxToolbarListener() {
        final CheckBox checkBoxDisplayToolbar = fragmentAppearanceBinding.checkBoxDisplayToolbar;
        checkBoxDisplayToolbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_APPEARANCE,
                    getResources().getResourceEntryName(checkBoxDisplayToolbar.getId()),
                    isChecked);

            setInstructionsDefault(isChecked);
        });
    }

    private void setInstructionsDefault(final boolean isChecked) {
        setToolbarInstructionsTint(isChecked, fragmentAppearanceBinding.textViewFirst);
        setToolbarInstructionsTint(isChecked, fragmentAppearanceBinding.textViewReport);
        setToolbarInstructionsTintFavourites(isChecked);
        setToolbarInstructionsTint(isChecked, fragmentAppearanceBinding.textViewShare);
        setToolbarInstructionsTint(isChecked, fragmentAppearanceBinding.textViewNew);
    }

    private void setToolbarInstructionsTint(final boolean isChecked, final TextView textView) {
        textView.setEnabled(isChecked);

        if (isChecked) {
            textView.getCompoundDrawables()[0].setTint(Color.BLACK);
        } else {
            textView.getCompoundDrawables()[0].setTint(Color.LTGRAY);
        }
    }

    private void setToolbarInstructionsTintFavourites(final boolean enable) {
        final TextView textViewFavouriteInstructions = fragmentAppearanceBinding.textViewFavourite;
        textViewFavouriteInstructions.setEnabled(enable);

        if (enable) {
            setTintOnTextView(textViewFavouriteInstructions, Color.BLACK);
        } else {
            setTintOnTextView(textViewFavouriteInstructions, Color.LTGRAY);
        }
    }

    private void setTintOnTextView(final TextView textView, final int tintColour) {
        final Drawable[] drawable = textView.getCompoundDrawables();

        // NullPointerException expected if tablet view rendering!
        if (tintColour == Color.BLACK) {
            drawable[0].setTint(tintColour);
            try {
                drawable[2].setTint(Color.RED);
            } catch (NullPointerException e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        } else {
            drawable[0].setTint(tintColour);
            try {
                drawable[2].setTint(tintColour);
            } catch (NullPointerException e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }
    }

    private void setSize() {
        final int[] sizeArray = getResources().getIntArray(R.array.fragment_appearance_size_array);
        final List<Integer> sizeIntegerArray = getSizeIntegerArray(sizeArray);

        final ArrayAdapter<Integer> spinnerArrayAdapter =
                new ArrayAdapter<Integer>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        sizeIntegerArray) {

                    @Override
                    public View getView(final int position, final View convertView, final ViewGroup parent) {
                        return getSizeDefault(
                                position,
                                super.getView(position, convertView, parent),
                                sizeIntegerArray);
                    }

                    @Override
                    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
                        return getSizeDefault(
                                position,
                                super.getDropDownView(position, convertView, parent),
                                sizeIntegerArray);
                    }
                };

        fragmentAppearanceBinding.spinnerSize.setAdapter(spinnerArrayAdapter);
        setSizeViaSharedPreference(sizeIntegerArray, fragmentAppearanceBinding.spinnerSize);
    }

    private void setSizeViaSharedPreference(final List<Integer> sizeIntegerArray, final Spinner spinnerSize) {
        final int sharedPreferenceSpinnerSize
                = this.preferences.getSharedPreferenceInt(Preferences.FRAGMENT_APPEARANCE, Preferences.SPINNER_SIZE);
        if (sharedPreferenceSpinnerSize == -1) {
            spinnerSize.setSelection(2);
        } else {
            int selectionIndex = 0;
            for (final Integer sizeInteger : sizeIntegerArray) {
                if (sharedPreferenceSpinnerSize == sizeInteger) {
                    spinnerSize.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }

    private View getSizeDefault(final int position, final View view, final List<Integer> sizeIntegerArray) {
        final TextView textView = (TextView) view;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeIntegerArray.get(position).floatValue());
        return view;
    }

    private List<Integer> getSizeIntegerArray(final int[] sizeArray) {
        final List<Integer> sizeIntegerArray = new ArrayList<>();
        for (final int size : sizeArray) {
            final Integer integer = size;
            sizeIntegerArray.add(integer);
        }
        return sizeIntegerArray;
    }

    private void setTransparency() {
        final int sharedPreferenceSeekBarProgress = preferences.getSharedPreferenceInt(
                Preferences.FRAGMENT_APPEARANCE,
                Preferences.SEEK_BAR);

        if (sharedPreferenceSeekBarProgress == -1) {
            fragmentAppearanceBinding.seekBarTransparency.setProgress(0);
        } else {
            fragmentAppearanceBinding.seekBarTransparency.setProgress(sharedPreferenceSeekBarProgress);
        }
    }

    private void setColour() {
        final Spinner spinnerColour = fragmentAppearanceBinding.spinnerColour;
        spinnerColour.setAdapter(new ColourSpinnerAdapter(getActivity().getBaseContext()));

        final String sharedPreferenceSpinnerColour = this.preferences.getSharedPreferenceString(
                Preferences.FRAGMENT_APPEARANCE, Preferences.SPINNER_COLOUR);
        if ("".equals(sharedPreferenceSpinnerColour)) {
            spinnerColour.setSelection(0);
        } else {
            int selectionIndex = 0;
            for (final String colour : getActivity().getBaseContext().getResources().getStringArray(R.array.fragment_appearance_colour_array)) {
                if (colour.equals(sharedPreferenceSpinnerColour)) {
                    spinnerColour.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }
}
