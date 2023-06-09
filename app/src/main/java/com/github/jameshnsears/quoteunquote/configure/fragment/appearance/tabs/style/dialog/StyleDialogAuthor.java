package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog;

import static android.view.View.VISIBLE;

import android.widget.Spinner;

import com.github.jameshnsears.quoteunquote.R;
import com.skydoves.colorpickerview.ColorEnvelope;

public class StyleDialogAuthor extends StyleDialogFragment {
    public StyleDialogAuthor(int widgetId) {
        super(widgetId, R.string.fragment_appearance_style_text_dialog_author);
        this.titleId = R.string.fragment_appearance_style_text_dialog_author_colour;
    }

    @Override
    public void showSwitchHide() {
        super.showSwitchHide();
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor.setVisibility(VISIBLE);
    }

    @Override
    public void createListenerTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor.setOnCheckedChangeListener((buttonView, isChecked) ->
                hideText = isChecked
        );
    }

    public void setTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor
                .setChecked(appearancePreferences.getAppearanceAuthorTextHide());
        hideText = appearancePreferences.getAppearanceAuthorTextHide();
    }

    @Override
    public void sharedPreferenceSaveTextSize(Spinner spinner) {
        appearancePreferences.setAppearanceAuthorTextSize(Integer.parseInt(spinner.getSelectedItem().toString()));
    }

    @Override
    public int sharedPreferenceGetTextSize() {
        return appearancePreferences.getAppearanceAuthorTextSize();
    }

    @Override
    public void sharedPreferenceSaveTextColour(ColorEnvelope envelope) {
        appearancePreferences.setAppearanceAuthorTextColour("#" + envelope.getHexCode());
    }

    @Override
    public String sharedPreferenceGetTextColour() {
        return appearancePreferences.getAppearanceAuthorTextColour();
    }

    public void sharedPreferenceSetTextHide(boolean isChecked) {
        appearancePreferences.setAppearanceAuthorTextHide(isChecked);
    }
}
