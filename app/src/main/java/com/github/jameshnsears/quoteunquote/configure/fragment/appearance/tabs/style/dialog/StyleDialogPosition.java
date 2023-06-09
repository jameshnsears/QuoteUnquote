package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog;

import static android.view.View.VISIBLE;

import android.widget.Spinner;

import com.github.jameshnsears.quoteunquote.R;
import com.skydoves.colorpickerview.ColorEnvelope;

public class StyleDialogPosition extends StyleDialogFragment {
    public StyleDialogPosition(int widgetId) {
        super(widgetId, R.string.fragment_appearance_style_text_dialog_position);
        this.titleId = R.string.fragment_appearance_style_text_dialog_position_colour;
    }

    @Override
    public void showSwitchHide() {
        super.showSwitchHide();
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition.setVisibility(VISIBLE);
    }

    @Override
    public void createListenerTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition.setOnCheckedChangeListener((buttonView, isChecked) ->
                hideText = isChecked
        );
    }

    public void setTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition
                .setChecked(appearancePreferences.getAppearancePositionTextHide());
        hideText = appearancePreferences.getAppearancePositionTextHide();
    }

    @Override
    public void sharedPreferenceSaveTextSize(Spinner spinner) {
        appearancePreferences.setAppearancePositionTextSize(Integer.parseInt(spinner.getSelectedItem().toString()));
    }

    @Override
    public int sharedPreferenceGetTextSize() {
        return appearancePreferences.getAppearancePositionTextSize();
    }

    @Override
    public void sharedPreferenceSaveTextColour(ColorEnvelope envelope) {
        appearancePreferences.setAppearancePositionTextColour("#" + envelope.getHexCode());
    }

    @Override
    public String sharedPreferenceGetTextColour() {
        return appearancePreferences.getAppearancePositionTextColour();
    }

    public void sharedPreferenceSetTextHide(boolean isChecked) {
        appearancePreferences.setAppearancePositionTextHide(isChecked);
    }
}
