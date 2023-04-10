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
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBrowseFavouritesDialogBinding;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrowseFavouritesDialogFragment extends DialogFragment {
    @Nullable
    public FragmentQuotationsBrowseFavouritesDialogBinding fragmentQuotationsBrowseFavouritesDialogBinding;

    @Nullable
    protected QuoteUnquoteModel quoteUnquoteModel;

    protected int title;

    public BrowseFavouritesDialogFragment(QuoteUnquoteModel quoteUnquoteModel, int title) {
        this.quoteUnquoteModel = quoteUnquoteModel;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        fragmentQuotationsBrowseFavouritesDialogBinding
                = FragmentQuotationsBrowseFavouritesDialogBinding.inflate(inflater.cloneInContext(
                new ContextThemeWrapper(
                        getActivity(), R.style.Theme_MaterialComponents_DayNight)));

        builder.setTitle(title);

        constructRecyclerView();

        builder.setView(fragmentQuotationsBrowseFavouritesDialogBinding.getRoot());
        builder.setNegativeButton(R.string.fragment_appearance_cancel, (dialog, id) -> getDialog().cancel());

        return builder.create();
    }

    private void constructRecyclerView() {
        fragmentQuotationsBrowseFavouritesDialogBinding.recycleViewFavourites
                .setLayoutManager(new LinearLayoutManager(getActivity()));

        // doesn't look good when RecyclerView has rounded corners!
        RecyclerView.ItemDecoration divider
                = new BrowseDividerItemDecorator(ContextCompat.getDrawable(getActivity(), R.drawable.recyclerview_row_favourites_divider));

        fragmentQuotationsBrowseFavouritesDialogBinding.recycleViewFavourites.addItemDecoration(divider);

        fragmentQuotationsBrowseFavouritesDialogBinding.recycleViewFavourites.addItemDecoration(divider);

        BrowseFavouritesAdapter adapter = new BrowseFavouritesAdapter(getFavourites());

        fragmentQuotationsBrowseFavouritesDialogBinding.recycleViewFavourites.setAdapter(adapter);
    }

    @NonNull
    private List<BrowseFavouritesData> getFavourites() {
        List<BrowseFavouritesData> browseFavouritesList = new ArrayList<>();

        List<QuotationEntity> favouriteQuotationsList = quoteUnquoteModel.getFavourites();
        int padding = String.valueOf(favouriteQuotationsList.size()).length();

        int index = 1;
        for (QuotationEntity favouriteQuotation: favouriteQuotationsList) {
            browseFavouritesList.add(new BrowseFavouritesData(
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
        fragmentQuotationsBrowseFavouritesDialogBinding = null;
    }
}
