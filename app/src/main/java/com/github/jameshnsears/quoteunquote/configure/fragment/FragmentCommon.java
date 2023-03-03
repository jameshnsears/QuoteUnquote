package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentCommon extends Fragment {
    public int widgetId;

    public FragmentCommon() {
        // ...
    }

    public FragmentCommon(final int theWidgetId) {
        widgetId = theWidgetId;
    }

    public void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }
}
