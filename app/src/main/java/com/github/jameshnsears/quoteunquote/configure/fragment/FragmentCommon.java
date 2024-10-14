package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import timber.log.Timber;

public class FragmentCommon extends Fragment {
    public int widgetId;

    public FragmentCommon() {
        // ...
    }

    public FragmentCommon(final int theWidgetId) {
        widgetId = theWidgetId;
        Timber.d("widgetId=%d", widgetId);
    }

    public void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }
}
