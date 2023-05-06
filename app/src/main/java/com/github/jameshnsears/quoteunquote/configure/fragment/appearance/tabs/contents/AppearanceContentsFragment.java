package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
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
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogAuthor;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogPosition;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogQuotation;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceTabContentsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import timber.log.Timber;

@Keep
public class AppearanceContentsFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceTabContentsBinding fragmentAppearanceTabContentsBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    public AppearancePreferences appearancePreferences;

    enum TextStyle {
        Quotation,
        Author,
        Position
    }

    public AppearanceContentsFragment() {
        // dark mode support
    }

    public AppearanceContentsFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceContentsFragment newInstance(final int widgetId) {
        final AppearanceContentsFragment fragment = new AppearanceContentsFragment(widgetId);
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

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());

        fragmentAppearanceTabContentsBinding
                = FragmentAppearanceTabContentsBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        getActivity(), com.google.android.material.R.style.Theme_MaterialComponents_DayNight)));
        return fragmentAppearanceTabContentsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceTabContentsBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull Bundle savedInstanceState) {
        createListenerBackgroundColourPicker();
        createListenerTransparency();

        createListenerTextFamily();
        createListenerTextStyle();
        createListenerForceItalicRegular();
        createListenerCenter();

        createListenerButtonQuotation();
        createListenerButtonAuthor();
        createListenerButtonPosition();

        setBackgroundColour();
        setTransparency();
        setTextFamily();
        setTextStyle();
        setTextForceItalicRegular();
        setTextCenter();

        setTextQuotation();
        setTextAuthor();
        setTextPosition();
    }

    public void setBackgroundColour() {
        String appearanceColour = appearancePreferences.getAppearanceColour();
        appearanceColour = appearanceColour.replace("#", "");
        int appearanceColourUnsignedInt = Integer.parseUnsignedInt(appearanceColour, 16);
        fragmentAppearanceTabContentsBinding
                .backgroundColourPickerButton.setBackgroundColor(appearanceColourUnsignedInt);

        setTextViewBackgroundColour(appearanceColourUnsignedInt);
    }

    private void setTextViewBackgroundColour(int appearanceColourUnsignedInt) {
        fragmentAppearanceTabContentsBinding.textViewCurrentQuotation
                .setBackgroundColor(appearanceColourUnsignedInt);
        fragmentAppearanceTabContentsBinding.textViewCurrentAuthor
                .setBackgroundColor(appearanceColourUnsignedInt);
        fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                .setBackgroundColor(appearanceColourUnsignedInt);
    }

    private void createListenerBackgroundColourPicker() {
        fragmentAppearanceTabContentsBinding.backgroundColourPickerButton.setOnClickListener(v -> {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext())
                    .setTitle(getString(R.string.fragment_appearance_background_colour_dialog_title))
                    .setPositiveButton(getString(R.string.fragment_appearance_ok),
                            (ColorEnvelopeListener) (envelope, fromUser) -> {

                                fragmentAppearanceTabContentsBinding
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
        fragmentAppearanceTabContentsBinding.seekBarTransparency.addOnSliderTouchListener(
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
        final Spinner spinner = fragmentAppearanceTabContentsBinding.spinnerFamily;
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
        final Spinner spinner = fragmentAppearanceTabContentsBinding.spinnerStyle;
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
        fragmentAppearanceTabContentsBinding.switchForceItalicRegular.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appearancePreferences.setAppearanceTextForceItalicRegular(isChecked);

            if (appearancePreferences.getAppearanceTextForceItalicRegular()) {
                fragmentAppearanceTabContentsBinding.spinnerStyle.setEnabled(false);
            } else {
                fragmentAppearanceTabContentsBinding.spinnerStyle.setEnabled(true);
            }

            setTextQuotation();
            setTextAuthor();
            setTextPosition();
        });
    }

    private void createListenerCenter() {
        fragmentAppearanceTabContentsBinding.switchCenter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appearancePreferences.setAppearanceTextCenter(isChecked);

            setTextQuotation();
            setTextAuthor();
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
        fragmentAppearanceTabContentsBinding.buttonQuotation.setOnClickListener(v -> {
            AppearanceTextDialogQuotation appearanceTextDialogQuotation
                    = new AppearanceTextDialogQuotation(widgetId);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerButtonAuthor() {
        fragmentAppearanceTabContentsBinding.buttonAuthor.setOnClickListener(v -> {
            AppearanceTextDialogAuthor appearanceTextDialogAuthor
                    = new AppearanceTextDialogAuthor(widgetId);
            appearanceTextDialogAuthor.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerButtonPosition() {
        fragmentAppearanceTabContentsBinding.buttonPosition.setOnClickListener(v -> {
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
            fragmentAppearanceTabContentsBinding.seekBarTransparency.setValue(0);
        } else {
            fragmentAppearanceTabContentsBinding.seekBarTransparency.setValue(transparency);
        }
    }

    public void setTextFamily() {
        setSpinner(
                fragmentAppearanceTabContentsBinding.spinnerFamily,
                new AppearanceTextFamilySpinnerAdapter(getActivity().getBaseContext()),
                sharedPreferenceGetTextFamily(),
                2,
                R.array.fragment_appearance_family_array
        );

        sharedPreferenceSaveTextFamily(fragmentAppearanceTabContentsBinding.spinnerFamily.getSelectedItem().toString());
    }

    public void setTextStyle() {
        setSpinner(
                fragmentAppearanceTabContentsBinding.spinnerStyle,
                new AppearanceTextStyleSpinnerAdapter(getActivity().getBaseContext()),
                sharedPreferenceGetTextStyle(),
                3,
                R.array.fragment_appearance_style_array
        );

        sharedPreferenceSaveTextStyle(fragmentAppearanceTabContentsBinding.spinnerStyle);
    }

    public void setTextForceItalicRegular() {
        fragmentAppearanceTabContentsBinding.switchForceItalicRegular
                .setChecked(appearancePreferences.getAppearanceTextForceItalicRegular());

        if (appearancePreferences.getAppearanceTextForceItalicRegular()) {
            fragmentAppearanceTabContentsBinding.spinnerStyle.setEnabled(false);
        } else {
            fragmentAppearanceTabContentsBinding.spinnerStyle.setEnabled(true);
        }
    }

    public void setTextCenter() {
        fragmentAppearanceTabContentsBinding.switchCenter
                .setChecked(appearancePreferences.getAppearanceTextCenter());

        if (appearancePreferences.getAppearanceTextCenter()) {
            fragmentAppearanceTabContentsBinding.textViewCurrentQuotation.setGravity(Gravity.CENTER);
            fragmentAppearanceTabContentsBinding.textViewCurrentAuthor.setGravity(Gravity.CENTER);
        } else {
            fragmentAppearanceTabContentsBinding.textViewCurrentQuotation.setGravity(Gravity.START | Gravity.CENTER);
            fragmentAppearanceTabContentsBinding.textViewCurrentAuthor.setGravity(Gravity.START | Gravity.CENTER);
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
        fragmentAppearanceTabContentsBinding.textViewCurrentQuotation
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearanceQuotationTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabContentsBinding.textViewCurrentQuotation,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Quotation);

        fragmentAppearanceTabContentsBinding.textViewCurrentQuotation
                .setTextSize(appearancePreferences.getAppearanceQuotationTextSize());

        setTextCenter();

        fragmentAppearanceTabContentsBinding.textViewCurrentQuotation
                .setText(R.string.fragment_appearance_button_quotation);
    }

    public void setTextAuthor() {
        fragmentAppearanceTabContentsBinding.textViewCurrentAuthor
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearanceAuthorTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabContentsBinding.textViewCurrentAuthor,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Author);

        fragmentAppearanceTabContentsBinding.textViewCurrentAuthor
                .setTextSize(appearancePreferences.getAppearanceAuthorTextSize());

        setTextCenter();

        SpannableString content;
        if (appearancePreferences.getAppearanceAuthorTextHide()) {
            content = new SpannableString("");
        } else {
            content = new SpannableString(getString(R.string.fragment_appearance_button_author));
        }
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        fragmentAppearanceTabContentsBinding.textViewCurrentAuthor
                .setText(content);

        showHideIconOnButton(
                appearancePreferences.getAppearanceAuthorTextHide(),
                fragmentAppearanceTabContentsBinding.buttonAuthor);
    }

    public void setTextPosition() {
        fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                .setTextColor(
                        Color.parseColor(appearancePreferences.getAppearancePositionTextColour())
                );

        setTextStyle(
                fragmentAppearanceTabContentsBinding.textViewCurrentPosition,
                getTextFamily(appearancePreferences.getAppearanceTextFamily()),
                appearancePreferences.getAppearanceTextStyle(),
                appearancePreferences.getAppearanceTextForceItalicRegular(),
                TextStyle.Position);

        fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                .setTextSize(appearancePreferences.getAppearanceAuthorTextSize());

        fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                .setTextSize(appearancePreferences.getAppearancePositionTextSize());

        if (appearancePreferences.getAppearancePositionTextHide()) {
            fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                    .setText("");
        } else {
            fragmentAppearanceTabContentsBinding.textViewCurrentPosition
                    .setText(R.string.fragment_appearance_button_position);
        }

        showHideIconOnButton(
                appearancePreferences.getAppearancePositionTextHide(),
                fragmentAppearanceTabContentsBinding.buttonPosition);
    }

    public void showHideIconOnButton(boolean appearancePositionTextHide, Button button) {
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
            default: // case "Regular":
                textView.setTypeface(typeFace, Typeface.NORMAL);
                break;
        }
    }
}
