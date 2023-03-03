package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.database.QuotationsDatabaseFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.QuotationsFilterFragment;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

public class QuotationsFragmentStateAdapter extends FragmentStateAdapter {
    private final int widgetId;

    public static QuotationsFilterFragment quotationsFilterFragment;

    public QuotationsFragmentStateAdapter(@NonNull final QuotationsFragment fa, final int widgetId) {
        super(fa);
        this.widgetId = widgetId;

        quotationsFilterFragment = QuotationsFilterFragment.newInstance(widgetId);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int pos) {
        switch (pos) {
            case 0:
                return quotationsFilterFragment;

            default:
                return QuotationsDatabaseFragment.newInstance(widgetId, quotationsFilterFragment);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
