package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseSourceDialogFragment extends BrowseFavouritesDialogFragment {
    public BrowseSourceDialogFragment(int widgetId, QuoteUnquoteModel quoteUnquoteModel, String title) {
        super(widgetId, quoteUnquoteModel, title);
        this.dialogType = BrowseAdapter.DIALOG.SOURCE;
    }

    @Override
    protected void constructRecyclerView() {
        super.constructRecyclerView();
    }

    @NonNull
    @Override
    protected List<BrowseData> getDataForRecyclerView() {
        List<BrowseData> browseSearchList = new ArrayList<>();

        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getActivity());

        List<QuotationEntity> searchQuotationsList = quoteUnquoteModel.getQuotationsForAuthor(quotationsPreferences.getContentSelectionAuthor());
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
}
