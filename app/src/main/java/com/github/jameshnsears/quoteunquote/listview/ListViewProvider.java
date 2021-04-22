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
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    private final QuotationEntity quotationEntity;
    @Nullable
    private String quotationPosition;
    private boolean isReported;
    private final int textSize;
    @Nullable
    private String textColour;

    @Nullable
    public QuoteUnquoteModel getQuoteUnquoteModel() {
        return quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }

    ListViewProvider(@NonNull final Context context, @NonNull final Intent intent) {
        synchronized (this) {
            this.context = context;

            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            Timber.d("%d", widgetId);

            AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);
            textSize = appearancePreferences.getAppearanceTextSize();
            textColour = appearancePreferences.getAppearanceTextColour();

            ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

            setQuoteUnquoteModel(new QuoteUnquoteModel(context));

            quotationEntity = getQuoteUnquoteModel().getCurrentQuotation(
                    widgetId);

            if (quotationEntity != null) {
                Timber.d("digest=%s", quotationEntity.digest);

                quotationPosition = getQuoteUnquoteModel().getCurrentPosition(
                        widgetId,
                        contentPreferences);

                isReported = getQuoteUnquoteModel().isReported(widgetId);
            }
        }
    }

    @Override
    public void onCreate() {
        // ...
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("%d", widgetId);

        synchronized (this) {
            if (quotationList.isEmpty()) {
                // first time call
                quotationList.add(getTheQuotation());
            } else {
                // subsequent calls
                if (!"".equals(getTheQuotation())) {
                    if (!quotationList.get(0).equals(getTheQuotation())) {
                        quotationList.set(0, getTheQuotation());
                    }
                }
            }
        }
    }

    @NonNull
    public String getTheQuotation() {
        if (quotationEntity == null) {
            return "";
        } else {
            return quotationEntity.theQuotation() + quotationPosition;
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
    public RemoteViews getViewAt(final int position) {
        final RemoteViews remoteViews = getRemoteViews(position);

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        remoteViews.setOnClickFillInIntent(R.id.textViewRow, intent);

        return remoteViews;
    }

    private int getRowLayoutId() {
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        String textFamily = appearancePreferences.getAppearanceTextFamily();
        String textStyle = appearancePreferences.getAppearanceTextStyle();
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
    private RemoteViews getRemoteViews(final int position) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), getRowLayoutId());

        remoteViews.setTextViewText(R.id.textViewRow, getTheQuotation());

        synchronized (this) {
            if (!quotationList.isEmpty() && !"".equals(getTheQuotation())) {
                remoteViews.setTextViewText(R.id.textViewRow, getTheQuotation());

                if (textSize != -1) {
                    remoteViews.setTextViewTextSize(
                            R.id.textViewRow,
                            TypedValue.COMPLEX_UNIT_DIP,
                            (float) textSize);
                }

                if (!textColour.equals("")) {
                    remoteViews.setTextColor(
                            R.id.textViewRow,
                            Color.parseColor(textColour));
                }

                int paintFlags = Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG;

                if (isReported) {
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
        return getRemoteViews(0);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
