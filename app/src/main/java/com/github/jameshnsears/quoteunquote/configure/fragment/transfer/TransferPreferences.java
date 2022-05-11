package com.github.jameshnsears.quoteunquote.configure.fragment.transfer;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class TransferPreferences extends PreferencesFacade {
    public TransferPreferences(@NonNull final Context applicationContext) {
        super(0, applicationContext);
    }

    @NonNull
    public String getTransferLocalCode() {
        return preferenceHelper.getPreferenceString(getLocalCode());
    }

    public void setTransferLocalCode(@NonNull final String value) {
        preferenceHelper.setPreference(getLocalCode(), value);
    }
}
