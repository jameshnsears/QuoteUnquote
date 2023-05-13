package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseFavouritesDialogFragment extends BrowseDialogFragment {
    public BrowseFavouritesDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final int titleResourceId) {
        super(widgetId, quoteUnquoteModel, titleResourceId);
        this.dialogType = BrowseAdapter.DIALOG.FAVOURITES;
    }

    public BrowseFavouritesDialogFragment(final int widgetId, final QuoteUnquoteModel quoteUnquoteModel, final String titleString, final int pageSize) {
        super(widgetId, quoteUnquoteModel, titleString);
        this.dialogType = BrowseAdapter.DIALOG.FAVOURITES;
        PAGE_SIZE = pageSize;
    }

    @NonNull
    protected List<BrowseData> setCachedRecyclerViewData() {
        final List<BrowseData> browseFavouritesList = new ArrayList<>();

        final List<QuotationEntity> favouriteQuotationsList = this.quoteUnquoteModel.getFavourites();
        final int padding = String.valueOf(favouriteQuotationsList.size()).length();

        int index = 1;
        for (final QuotationEntity favouriteQuotation : favouriteQuotationsList) {
            browseFavouritesList.add(new BrowseData(
                    Strings.padStart(String.valueOf(index), padding, '0'),
                    favouriteQuotation.quotation,
                    favouriteQuotation.author,
                    false,
                    favouriteQuotation.digest)
            );
            index += 1;
        }

        return browseFavouritesList;
    }
}
