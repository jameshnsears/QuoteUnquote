package com.github.jameshnsears.quoteunquote.configure.fragment.footer;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentFooterBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;


public final class FooterFragment extends FragmentCommon {
    @Nullable
    private FragmentFooterBinding fragmentFooterBinding;

    private FooterFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static FooterFragment newInstance(final int widgetId) {
        final FooterFragment fragment = new FooterFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
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
    public void onViewCreated(
            @NonNull final View view, final Bundle savedInstanceState) {
        fragmentFooterBinding.textViewVersion.setText(
                getResources().getString(R.string.fragment_footer_version,
                        BuildConfig.VERSION_NAME, BuildConfig.GIT_HASH));

        final LinearLayout layoutFooter = fragmentFooterBinding.layoutFooter;
        layoutFooter.setOnClickListener(v -> startActivity(IntentFactoryHelper.createIntentActionView()));
    }
}
