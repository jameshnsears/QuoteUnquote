package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.os.Bundle;
import android.util.Log;

import com.github.jameshnsears.quoteunquote.utils.Preferences;

import androidx.fragment.app.Fragment;

public class FragmentCommon extends Fragment {
    protected final int widgetId;
    protected Preferences preferences;

    protected FragmentCommon(final String logTag, final int widgetId) {
        Log.d(logTag, String.format("%d: %s", widgetId, logTag));
        this.widgetId = widgetId;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences(this.widgetId, this.getContext());
    }
}
