package com.github.jameshnsears.quoteunquote.configure.fragment;

import androidx.fragment.app.Fragment;

public class FragmentCommon extends Fragment {
    public int widgetId;

    public FragmentCommon() {
        // ...
    }

    protected FragmentCommon(final int theWidgetId) {
        super();
        widgetId = theWidgetId;
    }
}
