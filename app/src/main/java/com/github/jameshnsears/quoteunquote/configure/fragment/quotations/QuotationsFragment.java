package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

@Keep
public class QuotationsFragment extends FragmentCommon {
    @Nullable
    public FragmentQuotationsBinding fragmentQuotationsBinding;

    @Nullable
    private QuotationsFragmentStateAdapter pagerAdapter;

    public QuotationsFragment() {
        // dark mode support
    }

    public QuotationsFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsFragment newInstance(final int widgetId) {
        final QuotationsFragment fragment = new QuotationsFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final @NonNull ViewGroup container,
            final @NonNull Bundle savedInstanceState) {
        final Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_MaterialComponents_DayNight);

        fragmentQuotationsBinding = FragmentQuotationsBinding.inflate(inflater.cloneInContext(context));
        return fragmentQuotationsBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final @NonNull Bundle savedInstanceState) {

        pagerAdapter = new QuotationsFragmentStateAdapter(this, widgetId);
        fragmentQuotationsBinding.viewPager2Quotations.setAdapter(pagerAdapter);

        String[] tabs = {
                getString(R.string.fragment_quotations_selection),
                getString(R.string.fragment_quotations_database)
        };

        new TabLayoutMediator(
                fragmentQuotationsBinding.tabLayout,
                fragmentQuotationsBinding.viewPager2Quotations,
                true,
                false,
                (tab, position) -> tab.setText(tabs[position])).attach();
    }

    @Override
    public void onDestroyView() {
        fragmentQuotationsBinding = null;

        QuotationsFragmentStateAdapter.quotationsSelectionFragment.shutdown();
        QuotationsFragmentStateAdapter.quotationsSelectionFragment = null;

        super.onDestroyView();
    }
}
