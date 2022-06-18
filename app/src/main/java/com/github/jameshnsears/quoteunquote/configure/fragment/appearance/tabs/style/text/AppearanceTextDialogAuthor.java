package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.text;

import static android.view.View.VISIBLE;

import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;
import com.skydoves.colorpickerview.ColorEnvelope;

import timber.log.Timber;

public class AppearanceTextDialogAuthor extends AppearanceTextDialogFragment {
    public AppearanceTextDialogAuthor(int widgetId) {
        super(widgetId, R.string.fragment_appearance_style_text_dialog_author);
    }

    @Override
    public void showSwitchHide() {
        super.showSwitchHide();
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor.setVisibility(VISIBLE);
    }

    @Override
    public void createListenerTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferenceSetTextHide(isChecked)
        );
    }

    public void setTextHide() {
        fragmentAppearanceTabStyleDialogBinding.switchHideAuthor
                .setChecked(appearancePreferences.getAppearanceAuthorTextHide());
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
