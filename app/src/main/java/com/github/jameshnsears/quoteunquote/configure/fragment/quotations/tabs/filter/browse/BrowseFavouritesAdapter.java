package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import java.util.List;

import javax.annotation.Nullable;

import timber.log.Timber;

public class BrowseFavouritesAdapter extends RecyclerView.Adapter<BrowseFavouritesAdapter.ViewHolder> {
    static int widgetId;

    @Nullable
    static List<BrowseFavouritesData> browseFavouritesData;

    public BrowseFavouritesAdapter(int widgetId, List<BrowseFavouritesData> favourites) {
        this.widgetId = widgetId;
        browseFavouritesData = favourites;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView textViewSequentialIndex;
        private final TextView textViewQuotation;
        private final TextView textViewSource;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            textViewSequentialIndex = view.findViewById(R.id.textViewSequentialIndex);

            textViewQuotation = view.findViewById(R.id.editTextViewQuotation);

            textViewSource = view.findViewById(R.id.editTextViewSource);
        }

        public TextView getTextViewSequentialIndex() {
            return textViewSequentialIndex;
        }

        public TextView getTextViewQuotation() {
            return textViewQuotation;
        }

        public TextView getTextViewSource() {
            return textViewSource;
        }

        @Override
        public void onClick(View view) {
            Timber.d("favouritesBrowseRow=%d", getAdapterPosition());

            BrowseFavouritesData favourite = browseFavouritesData.get(getAdapterPosition());

            ConfigureActivity.launcherInvoked = true;

            AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, view.getContext());
            String toShare = favourite.getQuotation() + "\n\n" + favourite.getSource();
            if (appearancePreferences.getAppearanceToolbarShareNoSource()) {
                toShare = favourite.getQuotation();
            }

            view.getContext().startActivity(IntentFactoryHelper.createIntentShare(
                            view.getContext().getResources().getString(R.string.app_name), toShare
                    )
            );
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row_favourites, viewGroup, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        BrowseFavouritesData favourite = browseFavouritesData.get(position);

        viewHolder.getTextViewSequentialIndex().setText(favourite.getIndex());

        viewHolder.getTextViewQuotation().setText(favourite.getQuotation());
        viewHolder.getTextViewQuotation().setEnabled(false);

        viewHolder.getTextViewSource().setText(favourite.getSource());
        viewHolder.getTextViewSource().setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return browseFavouritesData.size();
    }
}
