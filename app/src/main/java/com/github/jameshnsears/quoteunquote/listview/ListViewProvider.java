package com.github.jameshnsears.quoteunquote.listview;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

class ListViewProvider implements RemoteViewsService.RemoteViewsFactory {
    @NonNull
    private final List<String> quotationList = new ArrayList<>();
    @NonNull
    private final Context context;
    private final int widgetId;
    @Nullable
    private final QuotationEntity quotationEntity;
    private final int textSize;
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    private String quotationPosition;
    private boolean isReported;
    @Nullable
    private final String textColour;

    ListViewProvider(@NonNull Context context, @NonNull Intent intent) {
        synchronized (this) {
            this.context = context;

            this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            Timber.d("%d", this.widgetId);

            final AppearancePreferences appearancePreferences = new AppearancePreferences(this.widgetId, context);
            this.textSize = appearancePreferences.getAppearanceTextSize();
            this.textColour = appearancePreferences.getAppearanceTextColour();

            final ContentPreferences contentPreferences = new ContentPreferences(this.widgetId, context);

            this.setQuoteUnquoteModel(new QuoteUnquoteModel(context));

            this.quotationEntity = this.getQuoteUnquoteModel().getCurrentQuotation(
                    this.widgetId);

            if (this.quotationEntity != null) {
                Timber.d("digest=%s", this.quotationEntity.digest);

                this.quotationPosition = this.getQuoteUnquoteModel().getCurrentPosition(
                        this.widgetId,
                        contentPreferences);

                this.isReported = this.getQuoteUnquoteModel().isReported(this.widgetId);
            }
        }
    }

    @Nullable
    public QuoteUnquoteModel getQuoteUnquoteModel() {
        return this.quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable final QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }

    @Override
    public void onCreate() {
        // ...
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("%d", this.widgetId);

        synchronized (this) {
            if (this.quotationList.isEmpty()) {
                // first time call
                this.quotationList.add(this.getTheQuotation());
            } else {
                // subsequent calls
                if (!"".equals(this.getTheQuotation())) {
                    if (!this.quotationList.get(0).equals(this.getTheQuotation())) {
                        this.quotationList.set(0, this.getTheQuotation());
                    }
                }
            }
        }
    }

    @NonNull
    public String getTheQuotation() {
        if (this.quotationEntity == null) {
            return "";
        } else {
            return this.quotationEntity.theQuotation() + this.quotationPosition;
        }
    }

    @Override
    public void onDestroy() {
        // ...
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    @NonNull
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = this.getRemoteViews(position);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widgetId);
        remoteViews.setOnClickFillInIntent(R.id.textViewRow, intent);

        return remoteViews;
    }

    private int getRowLayoutId() {
        final AppearancePreferences appearancePreferences = new AppearancePreferences(this.widgetId, this.context);

        final String textFamily = appearancePreferences.getAppearanceTextFamily();
        final String textStyle = appearancePreferences.getAppearanceTextStyle();
        Timber.d("textFamily=%s; textStyle=%s", textFamily, textStyle);

        int layoutId = 0;

        switch (textFamily) {
            case "Cursive":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForCursive(textStyle);
                break;

            case "Monospace":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForMonospace(textStyle);
                break;

            case "Sans Serif":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerif(textStyle);
                break;

            case "Sans Serif Condensed":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerifCondensed(textStyle);
                break;

            case "Sans Serif Medium":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerifMedium(textStyle);
                break;

            default:
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSerif(textStyle);
                break;
        }

        Timber.d("%d", layoutId);
        return layoutId;
    }

    @NonNull
    private RemoteViews getRemoteViews(int position) {
        RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), this.getRowLayoutId());

        remoteViews.setTextViewText(R.id.textViewRow, this.getTheQuotation());

        synchronized (this) {
            if (!this.quotationList.isEmpty() && !"".equals(this.getTheQuotation())) {
                remoteViews.setTextViewText(R.id.textViewRow, this.getTheQuotation());

                remoteViews.setTextViewTextSize(
                        R.id.textViewRow,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.textSize);

                remoteViews.setTextColor(
                        R.id.textViewRow,
                        Color.parseColor(this.textColour));

                final int paintFlags = Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG;

                if (this.isReported) {
                    remoteViews.setInt(R.id.textViewRow, "setPaintFlags",
                            paintFlags | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    remoteViews.setInt(R.id.textViewRow, "setPaintFlags", paintFlags);
                }
            }
        }

        return remoteViews;
    }

    @NonNull
    @Override
    public RemoteViews getLoadingView() {
        return this.getRemoteViews(0);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
