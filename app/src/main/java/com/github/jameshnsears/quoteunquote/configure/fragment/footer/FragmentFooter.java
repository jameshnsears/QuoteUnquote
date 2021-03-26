package com.github.jameshnsears.quoteunquote.configure.fragment.footer;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentFooterBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;


public final class FragmentFooter extends FragmentCommon {
    private static final String LOG_TAG = FragmentFooter.class.getSimpleName();

    private FragmentFooterBinding fragmentFooterBinding;

    private FragmentFooter(final int widgetId) {
        super(LOG_TAG, widgetId);
    }

    public static FragmentFooter newInstance(final int widgetId) {
        final FragmentFooter fragment = new FragmentFooter(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        fragmentFooterBinding = FragmentFooterBinding.inflate(getLayoutInflater());
        return fragmentFooterBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentFooterBinding = null;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        fragmentFooterBinding.textViewVersion.setText(
                getResources().getString(R.string.fragment_footer_version,
                        BuildConfig.VERSION_NAME, BuildConfig.GIT_HASH));

        final LinearLayout layoutFooter = fragmentFooterBinding.layoutFooter;
        layoutFooter.setOnClickListener(v -> startActivity(IntentFactoryHelper.createIntentActionView()));
    }
}
