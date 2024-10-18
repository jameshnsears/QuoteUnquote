package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.internal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabInternalBinding;

import timber.log.Timber;

@Keep
public class ContentInternalFragment extends ContentFragment {
    @Nullable
    public FragmentQuotationsTabDatabaseTabInternalBinding fragmentQuotationsTabDatabaseTabInternalBinding;

    @Nullable
    public QuotationsPreferences quotationsPreferences;

    public ContentInternalFragment(int widgetId) {
        super(widgetId);
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsTabDatabaseTabInternalBinding = FragmentQuotationsTabDatabaseTabInternalBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabDatabaseTabInternalBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {
        Timber.d("onViewCreated.ContentInternalFragment");

        if (this.quotationsPreferences.getDatabaseInternal()) {
            this.fragmentQuotationsTabDatabaseTabInternalBinding.radioButtonDatabaseInternal.setChecked(true);
        } else {
            this.fragmentQuotationsTabDatabaseTabInternalBinding.radioButtonDatabaseInternal.setChecked(false);
        }

        createListenerRadioInternalDatabase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.fragmentQuotationsTabDatabaseTabInternalBinding = null;
    }

    private void createListenerRadioInternalDatabase() {
        final RadioButton radioButtonDatabaseInternal = this.fragmentQuotationsTabDatabaseTabInternalBinding.radioButtonDatabaseInternal;
        radioButtonDatabaseInternal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useInternalDatabase();
        });
    }
}
