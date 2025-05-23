package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.QuotationsContentFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.QuotationsFilterFragment;

public class QuotationsFragmentStateAdapter extends FragmentStateAdapter {
    public static QuotationsFilterFragment quotationsFilterFragment;
    private final int widgetId;

    public QuotationsFragmentStateAdapter(@NonNull final QuotationsFragment fa, final int widgetId) {
        super(fa);
        this.widgetId = widgetId;
    }

    @NonNull
    @Override
    public Fragment createFragment(final int pos) {
        switch (pos) {
            case 0:
                return quotationsFilterFragment = QuotationsFilterFragment.newInstance(widgetId);

            default:
                return QuotationsContentFragment.newInstance(widgetId);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
