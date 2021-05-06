package com.github.jameshnsears.quoteunquote.configure.fragment.footer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentFooterBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

@Keep
public final class FooterFragment extends FragmentCommon {
    @Nullable
    private FragmentFooterBinding fragmentFooterBinding;

    public FooterFragment() {
        // dark mode support
    }

    public FooterFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static FooterFragment newInstance(int widgetId) {
        FooterFragment fragment = new FooterFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        this.fragmentFooterBinding = FragmentFooterBinding.inflate(this.getLayoutInflater());
        return this.fragmentFooterBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentFooterBinding = null;
    }

    @Override
    public void onViewCreated(
            @NonNull View view, Bundle savedInstanceState) {
        this.fragmentFooterBinding.textViewVersion.setText(
                this.getResources().getString(R.string.fragment_footer_version,
                        BuildConfig.VERSION_NAME, BuildConfig.GIT_HASH));

        LinearLayout layoutFooter = this.fragmentFooterBinding.layoutFooter;
        layoutFooter.setOnClickListener(v -> this.startActivity(IntentFactoryHelper.createIntentActionView()));
    }
}
