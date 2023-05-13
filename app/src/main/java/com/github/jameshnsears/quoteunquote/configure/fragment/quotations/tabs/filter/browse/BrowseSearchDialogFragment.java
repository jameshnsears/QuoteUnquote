package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BrowseSearchDialogFragment extends BrowseDialogFragment {
    private Set<String> favouriteDigests = new HashSet<>();

    public BrowseSearchDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final String title) {
        super(widgetId, quoteUnquoteModel, title);
        dialogType = BrowseAdapter.DIALOG.SEARCH;

        cacheFavourites();
    }

    private void cacheFavourites() {
        List<QuotationEntity> favourites = this.quoteUnquoteModel.getFavourites();
        for (QuotationEntity quotationEntity: favourites) {
            favouriteDigests.add(quotationEntity.digest);
        }
    }

    private boolean isFavourite(String digest) {
        if (favouriteDigests.contains(digest)) {
            return true;
        }

        return false;
    }

    @NonNull
    @Override
    protected List<BrowseData> setCachedRecyclerViewData() {
        final List<BrowseData> browseSearchList = new ArrayList<>();

        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getActivity());

        final List<QuotationEntity> searchQuotationsList;

        if (quotationsPreferences.getContentSelectionSearchRegEx()) {
            searchQuotationsList = this.quoteUnquoteModel.getSearchQuotationsRegEx(
                    quotationsPreferences.getContentSelectionSearch(),
                    quotationsPreferences.getContentSelectionSearchFavouritesOnly()
            );
        } else {
            searchQuotationsList = this.quoteUnquoteModel.getSearchQuotations(
                    quotationsPreferences.getContentSelectionSearch(),
                    quotationsPreferences.getContentSelectionSearchFavouritesOnly()
            );
        }

        final int padding = String.valueOf(searchQuotationsList.size()).length();

        int index = 1;
        ConcurrentLinkedDeque<BrowseData> list = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < searchQuotationsList.size(); i ++) {
            QuotationEntity searchQuotation = searchQuotationsList.get(i);

            BrowseData browseData = new BrowseData(
                    Strings.padStart(String.valueOf(index), padding, '0'),
                    searchQuotation.quotation,
                    searchQuotation.author,
                    isFavourite(searchQuotation.digest),
                    searchQuotation.digest);

            list.add(browseData);

            index += 1;
        }

        browseSearchList.addAll(list);

        return browseSearchList;
    }
}
