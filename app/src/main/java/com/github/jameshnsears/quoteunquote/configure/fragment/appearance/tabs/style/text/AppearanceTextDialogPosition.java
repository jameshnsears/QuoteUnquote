package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.text;

import static android.view.View.VISIBLE;

import android.widget.Spinner;

import com.github.jameshnsears.quoteunquote.R;
import com.skydoves.colorpickerview.ColorEnvelope;

public class AppearanceTextDialogPosition extends AppearanceTextDialogFragment {
    public AppearanceTextDialogPosition(int widgetId) {
        super(widgetId, R.string.fragment_appearance_style_text_dialog_position);
    }

    @Override
    public void showSwitchHide() {
        super.showSwitchHide();
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition.setVisibility(VISIBLE);
    }

    @Override
    public void createListenerTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferenceSetTextHide(isChecked)
        );
    }

    public void setTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHidePosition
                .setChecked(appearancePreferences.getAppearancePositionTextHide());
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
