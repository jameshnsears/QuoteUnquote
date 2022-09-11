package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabStyleDialogBinding;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AppearanceTextDialogFragment extends DialogFragment {
    protected int titleId;

    @Nullable
    public AppearancePreferences appearancePreferences;

    @Nullable
    public FragmentAppearanceTabStyleDialogBinding fragmentAppearanceTabStyleDialogBinding;

    protected int widgetId;

    protected int title;

    public AppearanceTextDialogFragment(int widgetId, int title) {
        Timber.d("%d", widgetId);
        this.widgetId = widgetId;
        this.title = title;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        fragmentAppearanceTabStyleDialogBinding
                = FragmentAppearanceTabStyleDialogBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        getActivity(), R.style.Theme_MaterialComponents_DayNight)));

        builder.setView(fragmentAppearanceTabStyleDialogBinding.getRoot())
                .setPositiveButton(R.string.fragment_appearance_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Bundle result = new Bundle();
                        // we don't populate the bundle
                        getParentFragmentManager().setFragmentResult("requestKey", result);
                        getDialog().dismiss();
                    }

                })
                .setNegativeButton(R.string.fragment_appearance_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        builder.setTitle(title);

        showSwitchHide();

        appearancePreferences = new AppearancePreferences(widgetId, getContext());

        createListenerTextColourPicker();
        createListenerTextSize();
        createListenerTextHide();

        setTextColour();
        setTextSize();
        setTextHide();

        return builder.create();
    }

    public void showSwitchHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor.setVisibility(View.GONE);
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceTabStyleDialogBinding = null;
    }

    public void createListenerTextHide() {
    }

    private void createListenerTextSize() {
        final Spinner spinner = fragmentAppearanceTabStyleDialogBinding.spinnerSize;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                sharedPreferenceSaveTextSize(spinner);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerTextColourPicker() {
        fragmentAppearanceTabStyleDialogBinding.textColourPickerButton.setOnClickListener(v -> {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext())
                    .setTitle(getString(titleId))
                    .setPositiveButton(getString(R.string.fragment_appearance_ok),
                            (ColorEnvelopeListener) (envelope, fromUser) -> {

                                fragmentAppearanceTabStyleDialogBinding
                                        .textColourPickerButton
                                        .setBackgroundColor(envelope.getColor());

                                sharedPreferenceSaveTextColour(envelope);
                            }

                    )
                    .setNegativeButton(getString(R.string.fragment_appearance_cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true);

            ColorPickerView colorPickerView = builder.getColorPickerView();

            String appearanceTextColour = sharedPreferenceGetTextColour();
            appearanceTextColour = appearanceTextColour.replace("#", "");
            int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceTextColour, 16);
            colorPickerView.setInitialColor(appearanceColourUnsignedInt);

            colorPickerView.getBrightnessSlider().invalidate();
            builder.show();
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

        fragmentAppearanceTabStyleDialogBinding.spinnerSize.setAdapter(spinnerArrayAdapter);
        setTextSizePreference(sizeIntegerArray, fragmentAppearanceTabStyleDialogBinding.spinnerSize);
    }

    private void setTextSizePreference(
            @NonNull final List<Integer> sizeIntegerArray,
            @NonNull final Spinner spinnerSize) {
        final int testSizePreference = sharedPreferenceGetTextSize();
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

    public void setTextColour() {
        String appearanceTextColour = sharedPreferenceGetTextColour();
        appearanceTextColour = appearanceTextColour.replace("#", "");
        fragmentAppearanceTabStyleDialogBinding
                .textColourPickerButton.setBackgroundColor(
                        Integer.parseUnsignedInt(appearanceTextColour, 16));
    }

    public void setTextHide() {
    }

    public void sharedPreferenceSaveTextSize(Spinner spinner) {
    }

    public int sharedPreferenceGetTextSize() {
        return -1;
    }

    public void sharedPreferenceSaveTextColour(ColorEnvelope envelope) {
    }

    @NonNull
    public String sharedPreferenceGetTextColour() {
        return "";
    }

    public void sharedPreferenceSetTextHide(boolean isChecked) {
    }
}
