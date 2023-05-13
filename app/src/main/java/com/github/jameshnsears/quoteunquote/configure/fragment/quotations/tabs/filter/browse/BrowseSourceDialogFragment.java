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

public class BrowseSourceDialogFragment extends BrowseDialogFragment {
    public BrowseSourceDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final String title) {
        super(widgetId, quoteUnquoteModel, title);
        this.dialogType = BrowseAdapter.DIALOG.SOURCE;
    }

    @NonNull
    @Override
    protected List<BrowseData> setCachedRecyclerViewData() {
        final List<BrowseData> browseSearchList = new ArrayList<>();

        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getActivity());

        final List<QuotationEntity> sourceQuotationsList = this.quoteUnquoteModel.getQuotationsForAuthor(quotationsPreferences.getContentSelectionAuthor());
        final int padding = String.valueOf(sourceQuotationsList.size()).length();

        int index = 1;
        for (final QuotationEntity sourceQuotation : sourceQuotationsList) {
            browseSearchList.add(new BrowseData(
                    Strings.padStart(String.valueOf(index), padding, '0'),
                    sourceQuotation.quotation,
                    sourceQuotation.author,
                    this.quoteUnquoteModel.isFavourite(sourceQuotation.digest),
                    sourceQuotation.digest)
            );
            index += 1;
        }

        return browseSearchList;
    }
}
