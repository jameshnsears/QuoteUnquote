package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter.BrowseDividerItemDecorator;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBrowseDialogBinding;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BrowseDialogFragment extends DialogFragment {
    @Nullable
    public FragmentQuotationsBrowseDialogBinding fragmentQuotationsBrowseDialogBinding;

    protected int widgetId;

    @Nullable
    protected QuoteUnquoteModel quoteUnquoteModel;

    protected BrowseAdapter.DIALOG dialogType = BrowseAdapter.DIALOG.BASE;

    protected int titleResourceId;

    protected String titleString;
    protected List<BrowseData> cachedRecyclerViewData;
    protected int PAGE_SIZE = 2000;
    BrowseAdapter adapter;
    int currentPage;

    public BrowseDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final int titleResourceId) {
        this.widgetId = widgetId;
        this.quoteUnquoteModel = quoteUnquoteModel;
        this.titleResourceId = titleResourceId;
    }

    public BrowseDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final String titleString) {
        this.widgetId = widgetId;
        this.quoteUnquoteModel = quoteUnquoteModel;
        this.titleString = titleString;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        final LayoutInflater inflater = this.requireActivity().getLayoutInflater();

        this.fragmentQuotationsBrowseDialogBinding
                = FragmentQuotationsBrowseDialogBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        this.getActivity(), com.google.android.material.R.style.Theme_MaterialComponents_DayNight)));

        if (null == titleString) {
            builder.setTitle(this.titleResourceId);
        } else {
            builder.setTitle(this.titleString);
        }

        this.constructRecyclerView();

        builder.setView(this.fragmentQuotationsBrowseDialogBinding.getRoot());

        return builder.create();
    }

    protected void constructRecyclerView() {
        this.fragmentQuotationsBrowseDialogBinding.recycleViewBrowse
                .setLayoutManager(new LinearLayoutManager(this.getActivity()));

        // doesn't look good when RecyclerView has rounded corners!
        final RecyclerView.ItemDecoration divider
                = new BrowseDividerItemDecorator(ContextCompat.getDrawable(this.getActivity(), R.drawable.recyclerview_row_divider));

        this.fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.addItemDecoration(divider);

        this.fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                final int lastVisiblePosition = 1 + layoutManager.findLastVisibleItemPosition();

                if (lastVisiblePosition < BrowseDialogFragment.this.cachedRecyclerViewData.size()) {
                    if (!recyclerView.canScrollVertically(1)) {
                        // load next page
                        BrowseDialogFragment.this.currentPage++;
                        Timber.d("currentPage=%d", BrowseDialogFragment.this.currentPage);

                        recyclerView.post(() ->
                                BrowseDialogFragment.this.adapter.addData(BrowseDialogFragment.this.getCachedRecyclerViewData(BrowseDialogFragment.this.currentPage), BrowseDialogFragment.this.adapter.getItemCount()));
                    }
                }
            }
        });

        this.cachedRecyclerViewData = this.setCachedRecyclerViewData();

        this.adapter = new BrowseAdapter(
                this.widgetId,
                this.getCachedRecyclerViewData(this.currentPage),
                this.dialogType);

        this.fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.setAdapter(this.adapter);
    }

    public List<BrowseData> getCachedRecyclerViewData(final int currentPage) {
        final int end = this.cachedRecyclerViewData.size();

        final int pageStart = currentPage * this.PAGE_SIZE;
        final int pageEnd = (pageStart + this.PAGE_SIZE) - 1;

        if (pageEnd <= end) {
            // move down a full page
            return this.cachedRecyclerViewData.subList(pageStart, pageStart + this.PAGE_SIZE);
        }

        // move down an incomplete page
        return this.cachedRecyclerViewData.subList(pageStart, end);
    }

    @NonNull
    protected List<BrowseData> setCachedRecyclerViewData() {
        return new ArrayList<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentQuotationsBrowseDialogBinding = null;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);

        final Fragment parentFragment = this.getParentFragment();
        if (parentFragment instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) parentFragment).onDismiss(dialog);
        }
    }
}
