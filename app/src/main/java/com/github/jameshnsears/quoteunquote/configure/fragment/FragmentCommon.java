package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.widget.BaseAdapter;
import android.widget.Spinner;

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
}
