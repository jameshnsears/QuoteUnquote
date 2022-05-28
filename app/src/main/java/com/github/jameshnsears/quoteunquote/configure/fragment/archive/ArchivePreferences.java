package com.github.jameshnsears.quoteunquote.configure.fragment.archive;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade;

public class ArchivePreferences extends PreferencesFacade {
    public static final String ARCHIVE_GOOGLE_CLOUD = "ARCHIVE_GOOGLE_CLOUD";
    public static final String ARCHIVE_SHARED_STORAGE = "ARCHIVE_SHARED_STORAGE";

    public ArchivePreferences(int widgetId, @NonNull final Context applicationContext) {
        super(widgetId, applicationContext);
    }

    @NonNull
    public String getTransferLocalCode() {
        return preferenceHelper.getPreferenceString(getLocalCode());
    }

    public void setTransferLocalCode(@NonNull final String value) {
        preferenceHelper.setPreference(getLocalCode(), value);
    }

    public boolean getArchiveGoogleCloud() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(ArchivePreferences.ARCHIVE_GOOGLE_CLOUD), true);
    }

    public void setArchiveGoogleCloud(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(ArchivePreferences.ARCHIVE_GOOGLE_CLOUD), value);
    }

    public boolean getArchiveSharedStorage() {
        return this.preferenceHelper.getPreferenceBoolean(this.getPreferenceKey(ArchivePreferences.ARCHIVE_SHARED_STORAGE), false);
    }

    public void setArchiveSharedStorage(boolean value) {
        this.preferenceHelper.setPreference(this.getPreferenceKey(ArchivePreferences.ARCHIVE_SHARED_STORAGE), value);
    }
}
