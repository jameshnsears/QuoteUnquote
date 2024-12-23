package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.viewpager2.widget.ViewPager2;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

@Keep
public class QuotationsFragment extends FragmentCommon implements DialogInterface.OnDismissListener {
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
        final Context context = new ContextThemeWrapper(getActivity(), com.google.android.material.R.style.Theme_MaterialComponents_DayNight);

        fragmentQuotationsBinding = FragmentQuotationsBinding.inflate(inflater.cloneInContext(context));
        return fragmentQuotationsBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final @NonNull Bundle savedInstanceState) {

        pagerAdapter = new QuotationsFragmentStateAdapter(this, widgetId);
        fragmentQuotationsBinding.viewPager2Quotations.setAdapter(pagerAdapter);
        fragmentQuotationsBinding.viewPager2Quotations.setUserInputEnabled(false);

        fragmentQuotationsBinding.viewPager2Quotations.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            // click on a tab once created...

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    Timber.d("onPageSelected=0");
                    // re-init the filter, in-case we have switched between internal / external databases
                    if (QuotationsFragmentStateAdapter.quotationsFilterFragment != null) {
                        if (QuotationsFragmentStateAdapter.quotationsFilterFragment.disposables.size() == 0) {
                            QuotationsFragmentStateAdapter.quotationsFilterFragment.disposables = new CompositeDisposable();
                            QuotationsFragmentStateAdapter.quotationsFilterFragment.initUI();
                            QuotationsFragmentStateAdapter.quotationsFilterFragment.setCard();
                        }
                    }

                } else {
                    Timber.d("onPageSelected=1");
                    if (QuotationsFragmentStateAdapter.quotationsFilterFragment != null) {
                        QuotationsFragmentStateAdapter.quotationsFilterFragment.shutdown();
                    }
                }
            }
        });

        // create the tabs first...
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

        String screen =
                new QuotationsPreferences(widgetId, getContext()).getScreen();

        if (screen.equals(Screen.ContentInternal.screenName)
                || screen.equals(Screen.ContentFiles.screenName)
                || screen.equals(Screen.ContentWeb.screenName)
        ) {
            fragmentQuotationsBinding.viewPager2Quotations.setCurrentItem(1);
        } else {
            fragmentQuotationsBinding.viewPager2Quotations.setCurrentItem(0);
        }
    }

    @Override
    public void onDestroyView() {
        fragmentQuotationsBinding = null;

        if (QuotationsFragmentStateAdapter.quotationsFilterFragment != null) {
            QuotationsFragmentStateAdapter.quotationsFilterFragment.shutdown();
            QuotationsFragmentStateAdapter.quotationsFilterFragment = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        QuotationsFragmentStateAdapter.quotationsFilterFragment.initUI();
    }
}
