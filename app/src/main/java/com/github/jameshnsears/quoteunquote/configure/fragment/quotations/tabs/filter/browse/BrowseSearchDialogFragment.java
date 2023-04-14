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

        /*
        quotations.db.dev
        1	a1	q1.1	Unit_testing	1624c314
        2	a1	q1.2	Unit_testing	50ffd352
        3	a1	q1.3	Unit_testing	51ffd352
        4	a1	q1.4	Unit_testing	52ffd352
        5	a1	q1.5	Unit_testing	53ffd352
        6	a2	a2.1	?	0c4e1c27
        7	a2	q2.2	?	bb4685f4
        8	a3	q3.1	?	1a2dbc82
        9	a3	q3.2	?	4c62b1d2
        10	a3	q3.3	?	87ea7602
         */

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
