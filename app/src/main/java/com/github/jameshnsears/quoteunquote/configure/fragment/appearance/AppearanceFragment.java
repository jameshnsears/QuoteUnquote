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
import androidx.viewpager2.widget.ViewPager2;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.databinding.FragmentAppearanceBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import timber.log.Timber;

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

        fragmentAppearanceBinding.viewPager2Appearance.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) {
                    Timber.d("onPageSelected=1");
                    AppearanceFragmentStateAdapter.appearanceToolbarFragment.setToolbarColour();
                }
            }
        });

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

        String screen =
                new QuotationsPreferences(widgetId, getContext()).getScreen();
        if (screen.equals(Screen.AppearanceToolbar.screenName)) {
            fragmentAppearanceBinding.viewPager2Appearance.setCurrentItem(1);
        } else {
            fragmentAppearanceBinding.viewPager2Appearance.setCurrentItem(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppearanceFragmentStateAdapter.appearanceToolbarFragment = null;
        fragmentAppearanceBinding = null;
    }
}
