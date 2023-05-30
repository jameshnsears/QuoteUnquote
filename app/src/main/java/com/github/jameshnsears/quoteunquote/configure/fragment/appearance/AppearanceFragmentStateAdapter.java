package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.AppearanceStyleFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.toolbar.AppearanceToolbarFragment;

public class AppearanceFragmentStateAdapter extends FragmentStateAdapter {
    private final int widgetId;

    public static AppearanceToolbarFragment appearanceToolbarFragment;

    public AppearanceFragmentStateAdapter(@NonNull final AppearanceFragment fa, final int widgetId) {
        super(fa);
        this.widgetId = widgetId;
        appearanceToolbarFragment = AppearanceToolbarFragment.newInstance(widgetId);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int pos) {
        switch (pos) {
            case 0:
                return AppearanceStyleFragment.newInstance(widgetId);

            default:
                return appearanceToolbarFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
