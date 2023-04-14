package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData;
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseDividerItemDecorator;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBrowseDialogBinding;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseFavouritesDialogFragment extends DialogFragment {
    @Nullable
    public FragmentQuotationsBrowseDialogBinding fragmentQuotationsBrowseDialogBinding;

    protected int widgetId;

    @Nullable
    protected QuoteUnquoteModel quoteUnquoteModel;

    protected int title;

    public BrowseFavouritesDialogFragment(int widgetId, QuoteUnquoteModel quoteUnquoteModel, int title) {
        this.widgetId = widgetId;
        this.quoteUnquoteModel = quoteUnquoteModel;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        fragmentQuotationsBrowseDialogBinding
                = FragmentQuotationsBrowseDialogBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        getActivity(), R.style.Theme_MaterialComponents_DayNight)));

        builder.setTitle(title);

        constructRecyclerView();

        builder.setView(fragmentQuotationsBrowseDialogBinding.getRoot());
        builder.setNegativeButton(R.string.fragment_appearance_cancel, (dialog, id) -> getDialog().cancel());

        return builder.create();
    }

    private void constructRecyclerView() {
        fragmentQuotationsBrowseDialogBinding.recycleViewBrowse
                .setLayoutManager(new LinearLayoutManager(getActivity()));

        // doesn't look good when RecyclerView has rounded corners!
        RecyclerView.ItemDecoration divider
                = new BrowseDividerItemDecorator(ContextCompat.getDrawable(getActivity(), R.drawable.recyclerview_row_divider));

        fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.addItemDecoration(divider);

        fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.addItemDecoration(divider);

        BrowseAdapter adapter = new BrowseAdapter(widgetId, getDataForRecyclerView());

        fragmentQuotationsBrowseDialogBinding.recycleViewBrowse.setAdapter(adapter);
    }

    @NonNull
    protected List<BrowseData> getDataForRecyclerView() {
        List<BrowseData> browseFavouritesList = new ArrayList<>();

        List<QuotationEntity> favouriteQuotationsList = quoteUnquoteModel.getFavourites();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentQuotationsBrowseDialogBinding = null;
    }
}
