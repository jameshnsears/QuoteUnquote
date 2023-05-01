package com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.util.List;

import javax.annotation.Nullable;

import timber.log.Timber;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.ViewHolder> {
    static int widgetId;

    public enum DIALOG {
        FAVOURITES,
        SEARCH,
        SOURCE
    }

    static DIALOG dialogType = DIALOG.FAVOURITES;

    @Nullable
    static List<BrowseData> browseDataList;

    public BrowseAdapter(final int widgetId,
                         final List<BrowseData> browseDataItems,
                         DIALOG dialogType) {
        BrowseAdapter.widgetId = widgetId;
        browseDataList = browseDataItems;
        this.dialogType = dialogType;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private final TextView textViewSequentialIndex;
        private final TextView textViewQuotation;
        private final TextView textViewSource;
        private TextView textViewFavourite = null;

        public ViewHolder(final View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            this.textViewSequentialIndex = view.findViewById(R.id.textViewSequentialIndex);

            this.textViewQuotation = view.findViewById(R.id.textViewQuotation);

            this.textViewSource = view.findViewById(R.id.textViewSource);

            this.textViewFavourite = view.findViewById(R.id.textViewFavourite);
        }

        public TextView getTextViewSequentialIndex() {
            return this.textViewSequentialIndex;
        }

        public TextView getTextViewQuotation() {
            return this.textViewQuotation;
        }

        public TextView getTextViewSource() {
            return this.textViewSource;
        }

        public TextView getTextViewFavourite() {
            return this.textViewFavourite;
        }

        @Override
        public void onClick(final View view) {
            BrowseData browseDataItem = BrowseAdapter.browseDataList.get(this.getAdapterPosition());

            ConfigureActivity.launcherInvoked = true;

            final AppearancePreferences appearancePreferences = new AppearancePreferences(BrowseAdapter.widgetId, view.getContext());
            String toShare = browseDataItem.getQuotation() + "\n\n" + browseDataItem.getSource();
            if (appearancePreferences.getAppearanceToolbarShareNoSource()) {
                toShare = browseDataItem.getQuotation();
            }

            view.getContext().startActivity(IntentFactoryHelper.createIntentShare(
                            view.getContext().getResources().getString(R.string.app_name), toShare
                    )
            );
        }

        @Override
        public boolean onLongClick(final View view) {
            BrowseData browseDataItem = browseDataList.get(this.getAdapterPosition());

            QuoteUnquoteModel quoteUnquoteModel = new QuoteUnquoteModel(widgetId, view.getContext());

            boolean isFavourite = quoteUnquoteModel.isFavourite(browseDataItem.getDigest());

            Timber.d("onLongClick: %d=%b", this.getAdapterPosition() + 1, isFavourite);

            if (isFavourite) {
                getTextViewFavourite().setVisibility(View.GONE);
                Toast toast = Toast.makeText(
                        view.getContext(),
                        view.getContext().getString(R.string.fragment_quotations_selection_dialog_browse_favourites_unfavourited),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 50);
                toast.show();
            } else {
                getTextViewFavourite().setVisibility(View.VISIBLE);
                Toast toast = Toast.makeText(
                        view.getContext(),
                        view.getContext().getString(R.string.fragment_quotations_selection_dialog_browse_favourites_favourited),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 50);
                toast.show();
            }

            quoteUnquoteModel.toggleFavourite(widgetId, browseDataItem.getDigest());

            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final BrowseData browseDataItem = BrowseAdapter.browseDataList.get(position);

        viewHolder.getTextViewSequentialIndex().setText(browseDataItem.getIndex());

        viewHolder.getTextViewQuotation().setText(browseDataItem.getQuotation());
        viewHolder.getTextViewQuotation().setEnabled(false);

        if (dialogType != DIALOG.SOURCE) {
            viewHolder.getTextViewSource().setText(browseDataItem.getSource());
            viewHolder.getTextViewSource().setEnabled(false);
        } else {
            viewHolder.getTextViewSource().setVisibility(View.GONE);
        }

        QuoteUnquoteModel quoteUnquoteModel = new QuoteUnquoteModel(widgetId, viewHolder.itemView.getContext());
        if (quoteUnquoteModel.isFavourite(browseDataItem.getDigest())) {
            viewHolder.getTextViewFavourite().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getTextViewFavourite().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return BrowseAdapter.browseDataList.size();
    }
}
