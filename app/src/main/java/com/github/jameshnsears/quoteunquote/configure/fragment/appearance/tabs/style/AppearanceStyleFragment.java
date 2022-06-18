package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.text.AppearanceTextDialogAuthor;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.text.AppearanceTextDialogPosition;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.text.AppearanceTextDialogQuotation;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabStyleBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import timber.log.Timber;

@Keep
public class AppearanceStyleFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceTabStyleBinding fragmentAppearanceTabStyleBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    public AppearancePreferences appearancePreferences;

    enum TextStyle {
        Quotation,
        Author,
        Position
    }

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createListenerTextDialogFragmentResult();
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        appearancePreferences = new AppearancePreferences(this.widgetId, getContext());

        quoteUnquoteModel = new QuoteUnquoteModel(getContext());

        fragmentAppearanceTabStyleBinding
                = FragmentAppearanceTabStyleBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        getActivity(), R.style.Theme_MaterialComponents_DayNight)));
        return fragmentAppearanceTabStyleBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceTabStyleBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull Bundle savedInstanceState) {
        createListenerBackgroundColourPicker();
        createListenerTransparency();

        createListenerTextFamily();
        createListenerTextStyle();
        createListenerForceItalicRegular();

        createListenerButtonQuotation();
        createListenerButtonAuthor();
        createListenerButtonPosition();

        setBackgroundColour();
        setTransparency();
        setTextFamily();
        setTextStyle();
        setTextForceItalicRegular();

        setTextQuotation();
        setTextAuthor();
        setTextPosition();
    }

    public void setBackgroundColour() {
        String appearanceColour = appearancePreferences.getAppearanceColour();
        appearanceColour = appearanceColour.replace("#", "");
        int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceColour, 16);
        fragmentAppearanceTabStyleBinding
                .backgroundColourPickerButton.setBackgroundColor(appearanceColourUnsignedInt);

        setTextViewBackgroundColour(appearanceColourUnsignedInt);
    }

    private void setTextViewBackgroundColour(int appearanceColourUnsignedInt) {
        fragmentAppearanceTabStyleBinding.textViewCurrentQuotation
                .setBackgroundColor(appearanceColourUnsignedInt);
        fragmentAppearanceTabStyleBinding.textViewCurrentAuthor
                .setBackgroundColor(appearanceColourUnsignedInt);
        fragmentAppearanceTabStyleBinding.textViewCurrentPosition
                .setBackgroundColor(appearanceColourUnsignedInt);
    }

    private void createListenerBackgroundColourPicker() {
        fragmentAppearanceTabStyleBinding.backgroundColourPickerButton.setOnClickListener(v -> {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext())
                    .setTitle(getString(R.string.fragment_appearance_background_colour_dialog_title))
                    .setPositiveButton(getString(R.string.fragment_appearance_ok),
                            (ColorEnvelopeListener) (envelope, fromUser) -> {

                                fragmentAppearanceTabStyleBinding
                                        .backgroundColourPickerButton
                                        .setBackgroundColor(envelope.getColor());

                                appearancePreferences.setAppearanceColour("#" + envelope.getHexCode());

                                setTextViewBackgroundColour(envelope.getColor());
                            }

                    )
                    .setNegativeButton(getString(R.string.fragment_appearance_cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true);

            ColorPickerView colorPickerView = builder.getColorPickerView();

            String appearanceColour = appearancePreferences.getAppearanceColour();
            appearanceColour = appearanceColour.replace("#", "");
            int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceColour, 16);
            colorPickerView.setInitialColor(appearanceColourUnsignedInt);

            colorPickerView.getBrightnessSlider().invalidate();
            builder.show();
        });
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
                sharedPreferenceSaveTextFamily(spinner.getSelectedItem().toString());

                setTextQuotation();
                setTextAuthor();
                setTextPosition();
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
                sharedPreferenceSaveTextStyle(spinner);

                setTextQuotation();
                setTextAuthor();
                setTextPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });
    }

    private void createListenerForceItalicRegular() {
        fragmentAppearanceTabStyleBinding.switchForceItalicRegular.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appearancePreferences.setAppearanceTextForceItalicRegular(isChecked);

            if (appearancePreferences.getAppearanceTextForceItalicRegular()) {
                fragmentAppearanceTabStyleBinding.spinnerStyle.setEnabled(false);
            } else {
                fragmentAppearanceTabStyleBinding.spinnerStyle.setEnabled(true);
            }

            setTextQuotation();
            setTextAuthor();
            setTextPosition();
        });
    }

    public void sharedPreferenceSaveTextFamily(String selectedItem) {
        if (!appearancePreferences.getAppearanceTextFamily().equals(selectedItem)) {
            appearancePreferences.setAppearanceTextFamily(selectedItem);
        }
    }

    @NonNull
    public String sharedPreferenceGetTextFamily() {
        return appearancePreferences.getAppearanceTextFamily();
    }

    public void sharedPreferenceSaveTextStyle(Spinner spinner) {
        appearancePreferences.setAppearanceTextStyle(spinner.getSelectedItem().toString());
    }

    @NonNull
    public String sharedPreferenceGetTextStyle() {
        return appearancePreferences.getAppearanceTextStyle();
    }

    private void createListenerButtonQuotation() {
        fragmentAppearanceTabStyleBinding.buttonQuotation.setOnClickListener(v -> {
            AppearanceTextDialogQuotation appearanceTextDialogQuotation
                    = new AppearanceTextDialogQuotation(widgetId);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerButtonAuthor() {
        fragmentAppearanceTabStyleBinding.buttonAuthor.setOnClickListener(v -> {
            AppearanceTextDialogAuthor appearanceTextDialogAuthor
                    = new AppearanceTextDialogAuthor(widgetId);
            appearanceTextDialogAuthor.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerButtonPosition() {
        fragmentAppearanceTabStyleBinding.buttonPosition.setOnClickListener(v -> {
            AppearanceTextDialogPosition appearanceTextDialogPosition
                    = new AppearanceTextDialogPosition(widgetId);
            appearanceTextDialogPosition.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerTextDialogFragmentResult() {
        getParentFragmentManager().setFragmentResultListener(
                "requestKey",
                this,
                (requestKey, bundle) -> {
                    setTextQuotation();
                    setTextAuthor();
                    setTextPosition();
                });
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
                sharedPreferenceGetTextFamily(),
                2,
                R.array.fragment_appearance_family_array
        );

        sharedPreferenceSaveTextFamily(fragmentAppearanceTabStyleBinding.spinnerFamily.getSelectedItem().toString());
    }

    public void setTextStyle() {
        setSpinner(
                fragmentAppearanceTabStyleBinding.spinnerStyle,
                new AppearanceTextStyleSpinnerAdapter(getActivity().getBaseContext()),
                sharedPreferenceGetTextStyle(),
                3,
                R.array.fragment_appearance_style_array
        );

        sharedPreferenceSaveTextStyle(fragmentAppearanceTabStyleBinding.spinnerStyle);
    }

    public void setTextForceItalicRegular() {
        fragmentAppearanceTabStyleBinding.switchForceItalicRegular
                .setChecked(appearancePreferences.getAppearanceTextForceItalicRegular());

        if (appearancePreferences.getAppearanceTextForceItalicRegular()) {
            fragmentAppearanceTabStyleBinding.spinnerStyle.setEnabled(false);
        } else {
            fragmentAppearanceTabStyleBinding.spinnerStyle.setEnabled(true);
        }
    }

    public void setSpinner(
            @NonNull final Spinner spinner,
            @NonNull final BaseAdapter spinnerAdapter,
            @NonNull final String preference,
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

    public void setTextQuotation() {
        fragmentAppearanceTabStyleBinding.textViewCurrentQuotation
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearanceQuotationTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabStyleBinding.textViewCurrentQuotation,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Quotation);

        fragmentAppearanceTabStyleBinding.textViewCurrentQuotation
                .setTextSize(appearancePreferences.getAppearanceQuotationTextSize());

        fragmentAppearanceTabStyleBinding.textViewCurrentQuotation
                .setText(R.string.fragment_appearance_button_quotation);
    }

    public void setTextAuthor() {
        fragmentAppearanceTabStyleBinding.textViewCurrentAuthor
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearanceAuthorTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabStyleBinding.textViewCurrentAuthor,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Author);

        fragmentAppearanceTabStyleBinding.textViewCurrentAuthor
                .setTextSize(appearancePreferences.getAppearanceAuthorTextSize());

        SpannableString content = new SpannableString(getString(R.string.fragment_appearance_button_author));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        fragmentAppearanceTabStyleBinding.textViewCurrentAuthor
                .setText(content);

        showHideIconOnbutton(
                appearancePreferences.getAppearanceAuthorTextHide(),
                fragmentAppearanceTabStyleBinding.buttonAuthor);
    }

    public void setTextPosition() {
        fragmentAppearanceTabStyleBinding.textViewCurrentPosition
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearancePositionTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabStyleBinding.textViewCurrentPosition,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Position);

        fragmentAppearanceTabStyleBinding.textViewCurrentPosition
                .setTextSize(appearancePreferences.getAppearanceAuthorTextSize());

        fragmentAppearanceTabStyleBinding.textViewCurrentPosition
                .setTextSize(appearancePreferences.getAppearancePositionTextSize());

        fragmentAppearanceTabStyleBinding.textViewCurrentPosition
                .setText(R.string.fragment_appearance_button_position);

        showHideIconOnbutton(
                appearancePreferences.getAppearancePositionTextHide(),
                fragmentAppearanceTabStyleBinding.buttonPosition);
    }

    public void showHideIconOnbutton(boolean appearancePositionTextHide, Button button) {
        if (appearancePositionTextHide) {
            Drawable icon
                    = ContextCompat.getDrawable(getContext(), R.drawable.ic_appearance_visibility_off_24);
            ((MaterialButton) button).setIcon(icon);
        } else {
            ((MaterialButton) button).setIcon(null);
        }
    }

    private Typeface getTextFamily(String family) {
        if (family.equals("Cursive")) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), "font/DancingScript_Regular.ttf");
        } else if (family.equals("Monospace")) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), "font/DroidSansMono.ttf");
        } else if (family.equals("Sans Serif")) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), "font/Roboto_Regular.ttf");
        } else if (family.equals("Sans Serif Condensed")) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), "font/RobotoCondensed_Regular.ttf");
        } else if (family.equals("Sans Serif Medium")) {
            return Typeface.createFromAsset(
                    getContext().getAssets(), "font/Roboto_Medium.ttf");
        }
        // "Serif":
        return Typeface.createFromAsset(
                getContext().getAssets(), "font/NotoSerif_Regular.ttf");

    }

    private void setTextStyle(
            TextView textView, Typeface typeFace, String style,
            boolean forceItalicRegular, TextStyle textStyle) {
        textView.setShadowLayer(0, 0, 0, 0);

        if (forceItalicRegular) {
            if (textStyle == TextStyle.Quotation) {
                textView.setTypeface(typeFace, Typeface.ITALIC);
            } else {
                textView.setTypeface(typeFace, Typeface.NORMAL);
            }
            return;
        }

        switch (style) {
            case "Bold":
                textView.setTypeface(typeFace, Typeface.BOLD);
                break;
            case "Bold Italic":
                textView.setTypeface(typeFace, Typeface.BOLD_ITALIC);
                break;
            case "Italic":
                textView.setTypeface(typeFace, Typeface.ITALIC);
                break;
            case "Italic, Shadow":
                textView.setTypeface(typeFace, Typeface.ITALIC);
                textView.setShadowLayer(1F, 2F, 2F, Color.BLACK);
                break;
            case "Regular, Shadow":
                textView.setTypeface(typeFace, Typeface.NORMAL);
                textView.setShadowLayer(1F, 2F, 2F, Color.BLACK);
                break;
            case "Regular":
                textView.setTypeface(typeFace, Typeface.NORMAL);
                break;
        }
    }
}
