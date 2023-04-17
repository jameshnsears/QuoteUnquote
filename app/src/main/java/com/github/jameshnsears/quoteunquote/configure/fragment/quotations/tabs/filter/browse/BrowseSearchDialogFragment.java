package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseSearchDialogFragment extends BrowseFavouritesDialogFragment {
    public BrowseSearchDialogFragment(int widgetId, QuoteUnquoteModel quoteUnquoteModel, int title) {
        super(widgetId, quoteUnquoteModel, title);
    }

    @NonNull
    @Override
    protected List<BrowseData> getDataForRecyclerView() {
        List<BrowseData> browseFavouritesList = new ArrayList<>();

        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getActivity());

        List<QuotationEntity> favouriteQuotationsList = quoteUnquoteModel.getSearchQuotations(
                quotationsPreferences.getContentSelectionSearch(),
                quotationsPreferences.getContentSelectionSearchFavouritesOnly()
        );
        int padding = String.valueOf(favouriteQuotationsList.size()).length();

        int index = 1;
        for (QuotationEntity favouriteQuotation: favouriteQuotationsList) {
            browseFavouritesList.add(new BrowseData(
                    Strings.padStart("" + index, padding, '0'),
                    favouriteQuotation.quotation,
                    favouriteQuotation.author));
            index += 1;
        }

        return browseFavouritesList;
    }
}
