package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class FragmentCommon extends Fragment {
    public final int widgetId;

    protected FragmentCommon(final int theWidgetId) {
        super();
        widgetId = theWidgetId;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
