package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.FilesFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.internal.ContentInternalFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.web.ContentWebFragment;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseBinding;
import com.google.android.material.tabs.TabLayout;

@Keep
public class QuotationsContentFragment extends FragmentCommon {
    @Nullable
    public FragmentQuotationsTabDatabaseBinding fragmentQuotationsTabDatabaseBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    public QuotationsPreferences quotationsPreferences;

    public QuotationsContentFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsContentFragment newInstance(
            int widgetId) {
        QuotationsContentFragment fragment = new QuotationsContentFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsTabDatabaseBinding = FragmentQuotationsTabDatabaseBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabDatabaseBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {

        loadFragment(new ContentInternalFragment(widgetId));

        fragmentQuotationsTabDatabaseBinding.tabDatabase.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        selectedFragment = new ContentInternalFragment(widgetId);
                        break;
                    case 1:
                        selectedFragment = new FilesFragment(widgetId);
                        break;
                    case 2:
                        selectedFragment = new ContentWebFragment(widgetId);
                        break;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselected if needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselected if needed
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.fragmentQuotationsTabDatabaseBinding = null;
    }
}
