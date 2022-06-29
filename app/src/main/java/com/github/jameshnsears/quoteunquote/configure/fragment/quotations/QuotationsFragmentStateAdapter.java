package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.database.QuotationsDatabaseFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.selection.QuotationsSelectionFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

public class QuotationsFragmentStateAdapter extends FragmentStateAdapter {
    private final int widgetId;

    public static QuotationsSelectionFragment quotationsSelectionFragment;

    public QuotationsFragmentStateAdapter(@NonNull final QuotationsFragment fa, final int widgetId) {
        super(fa);
        this.widgetId = widgetId;
    }

    @NonNull
    @Override
    public Fragment createFragment(final int pos) {
        if (quotationsSelectionFragment == null) {
            quotationsSelectionFragment = QuotationsSelectionFragment.newInstance(widgetId);
        }

        switch (pos) {
            case 0:
                return quotationsSelectionFragment;

            default:
                return QuotationsDatabaseFragment.newInstance(widgetId);
        }
    }

    public static void alignSelectionFragmentWithSelectedDatabase(int widgetId, @NonNull Context context) {
        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);
        quotationsPreferences.setContentSelection(ContentSelection.ALL);
        quotationsPreferences.setContentSelectionAuthor("");

        if (quotationsSelectionFragment != null) {
            quotationsSelectionFragment.initUi();
        }
    }

    public static void alignSelectionFragmentWithRestore(int widgetId, @NonNull Context context) {
        // we always move back to the Internal after a restore
        DatabaseRepository.useInternalDatabase = true;

        alignSelectionFragmentWithSelectedDatabase(widgetId, context);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
