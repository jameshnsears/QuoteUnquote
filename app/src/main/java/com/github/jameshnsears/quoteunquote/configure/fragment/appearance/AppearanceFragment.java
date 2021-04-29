package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AppearanceFragment extends FragmentCommon {
    @Nullable
    public FragmentAppearanceBinding fragmentAppearanceBinding;
    @Nullable
    private FragmentStateAdapter pagerAdapter;

    public AppearanceFragment() {
        // dark mode support
    }

    protected AppearanceFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static AppearanceFragment newInstance(int widgetId) {
        AppearanceFragment fragment = new AppearanceFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Context context = new ContextThemeWrapper(this.getActivity(), R.style.Theme_MaterialComponents_DayNight);

        this.fragmentAppearanceBinding = FragmentAppearanceBinding.inflate(inflater.cloneInContext(context));
        return this.fragmentAppearanceBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, Bundle savedInstanceState) {

        this.pagerAdapter = new AppearanceFragmentStateAdapter(this, this.widgetId);
        this.fragmentAppearanceBinding.viewPager2.setAdapter(this.pagerAdapter);

        final String[] tabs = {"Style", "Toolbar"};
        new TabLayoutMediator(
                this.fragmentAppearanceBinding.tabLayout,
                this.fragmentAppearanceBinding.viewPager2,
                true,
                false,
                (tab, position) -> tab.setText(tabs[position])).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentAppearanceBinding = null;
    }
}
