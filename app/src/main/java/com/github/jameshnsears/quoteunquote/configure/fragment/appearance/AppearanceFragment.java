package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceBinding;
import com.google.android.material.tabs.TabLayoutMediator;

@Keep
public class AppearanceFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceBinding fragmentAppearanceBinding;
    @Nullable
    private FragmentStateAdapter pagerAdapter;

    public AppearanceFragment() {
        // dark mode support
    }

    public AppearanceFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceFragment newInstance(final int widgetId) {
        final AppearanceFragment fragment = new AppearanceFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final @NonNull ViewGroup container,
            final @NonNull Bundle savedInstanceState) {
        final Context context = new ContextThemeWrapper(getActivity(), com.google.android.material.R.style.Theme_MaterialComponents_DayNight);

        fragmentAppearanceBinding = FragmentAppearanceBinding.inflate(inflater.cloneInContext(context));
        return fragmentAppearanceBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final @NonNull Bundle savedInstanceState) {

        pagerAdapter = new AppearanceFragmentStateAdapter(this, widgetId);
        fragmentAppearanceBinding.viewPager2Appearance.setAdapter(pagerAdapter);
        fragmentAppearanceBinding.viewPager2Appearance.setUserInputEnabled(false);

        String[] tabs = {
                getString(R.string.fragment_appearance_tab_style),
                getString(R.string.fragment_appearance_navigation)
        };
        new TabLayoutMediator(
                fragmentAppearanceBinding.tabLayout,
                fragmentAppearanceBinding.viewPager2Appearance,
                true,
                false,
                (tab, position) -> tab.setText(tabs[position])).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentAppearanceBinding = null;
    }
}
