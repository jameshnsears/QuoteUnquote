package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseSearchDialogFragment extends BrowseFavouritesDialogFragment {
    public BrowseSearchDialogFragment(int widgetId, QuoteUnquoteModel quoteUnquoteModel, int title) {
        super(widgetId, quoteUnquoteModel, title);
        this.dialogType = BrowseAdapter.DIALOG.SEARCH;
    }

    @Override
    protected void constructRecyclerView() {
        super.constructRecyclerView();
        fragmentQuotationsBrowseDialogBinding
                .textViewShareInfo.setText(R.string.fragment_quotations_selection_dialog_browse_share_and_toggle);
    }

    @NonNull
    @Override
    protected List<BrowseData> getDataForRecyclerView() {
        List<BrowseData> browseSearchList = new ArrayList<>();

        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getActivity());

        List<QuotationEntity> searchQuotationsList = quoteUnquoteModel.getSearchQuotations(
                quotationsPreferences.getContentSelectionSearch(),
                quotationsPreferences.getContentSelectionSearchFavouritesOnly()
        );
        int padding = String.valueOf(searchQuotationsList.size()).length();

        int index = 1;
        for (QuotationEntity searchQuotation: searchQuotationsList) {
            browseSearchList.add(new BrowseData(
                    Strings.padStart("" + index, padding, '0'),
                    searchQuotation.quotation,
                    searchQuotation.author,
                    quoteUnquoteModel.isFavourite(searchQuotation.digest),
                    searchQuotation.digest)
            );
            index += 1;
        }

        return browseSearchList;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment parentFragment = getParentFragment();

        if (parentFragment instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) parentFragment).onDismiss(dialog);
        }
    }
}
