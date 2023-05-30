package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog;

import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;
import com.skydoves.colorpickerview.ColorEnvelope;

public class StyleDialogQuotation extends StyleDialogFragment {
    public StyleDialogQuotation(int widgetId) {
        super(widgetId, R.string.fragment_appearance_style_text_dialog_quotation);
        this.titleId = R.string.fragment_appearance_style_text_dialog_quotation_colour;
    }

    @Override
    public void sharedPreferenceSaveTextSize(Spinner spinner) {
        appearancePreferences.setAppearanceQuotationTextSize(Integer.parseInt(spinner.getSelectedItem().toString()));
    }

    @Override
    public int sharedPreferenceGetTextSize() {
        return appearancePreferences.getAppearanceQuotationTextSize();
    }

    @Override
    public void sharedPreferenceSaveTextColour(ColorEnvelope envelope) {
        appearancePreferences.setAppearanceQuotationTextColour("#" + envelope.getHexCode());
    }

    @Override
    public String sharedPreferenceGetTextColour() {
        return appearancePreferences.getAppearanceQuotationTextColour();
    }

    @NonNull
    public String sharedPreferenceGetTextStyle() {
        return appearancePreferences.getAppearanceTextStyle();
    }
}
