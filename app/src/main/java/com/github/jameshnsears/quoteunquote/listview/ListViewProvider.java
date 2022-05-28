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
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

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
    private final String textColour;
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    private String quotationPosition;

    ListViewProvider(@NonNull final Context context, @NonNull final Intent intent) {
        synchronized (this) {
            this.context = context;

            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            Timber.d("%d", widgetId);

            AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);
            textSize = appearancePreferences.getAppearanceTextSize();
            textColour = appearancePreferences.getAppearanceTextColour();

            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

            setQuoteUnquoteModel(new QuoteUnquoteModel(context));

            quotationEntity = getQuoteUnquoteModel().getCurrentQuotation(
                    widgetId);

            if (quotationEntity != null) {
                Timber.d("digest=%s", quotationEntity.digest);

                quotationPosition = getQuoteUnquoteModel().getCurrentPosition(
                        widgetId,
                        quotationsPreferences);
                if (quotationEntity.digest.equals(
                        getQuoteUnquoteModel().getLastPreviousDigest(
                                widgetId, quotationsPreferences.getContentSelection()))) {

                    quotationPosition = "\u2316  " + quotationPosition;
                }
            }
        }
    }

    @Nullable
    public QuoteUnquoteModel getQuoteUnquoteModel() {
        return quoteUnquoteModel;
    }

    public void setQuoteUnquoteModel(@Nullable QuoteUnquoteModel quoteUnquoteModel) {
        this.quoteUnquoteModel = quoteUnquoteModel;
    }

    @Override
    public void onCreate() {
        // ...
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("%d", widgetId);

        synchronized (this) {
            if (quotationList.isEmpty() && quotationEntity != null) {
                // first time call
                quotationList.add(quotationEntity.theQuotation());
                quotationList.add(quotationEntity.theAuthor());
                quotationList.add(quotationPosition);
            } else {
                // subsequent calls
                if (quotationEntity != null && !"".equals(quotationEntity.theQuotation())
                        &&
                        !quotationList.get(0).equals(quotationEntity.theQuotation())) {
                    quotationList.set(0, quotationEntity.theQuotation());
                    quotationList.set(1, quotationEntity.theAuthor());
                    quotationList.set(2, quotationPosition);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        this.quoteUnquoteModel.databaseRepository.abstractHistoryDatabase = null;
        this.quoteUnquoteModel = null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    @NonNull
    public RemoteViews getViewAt(final int position) {
        final RemoteViews remoteViews = getRemoteViews(position);

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowQuotation,
                IntentFactoryHelper.createIntent(widgetId));

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowAuthor,
                IntentFactoryHelper.createClickFillInIntent(
                        "wikipedia",
                        quotationEntity.wikipedia,
                        this.widgetId));

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowPosition,
                IntentFactoryHelper.createIntent(widgetId));

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

        remoteViews.setTextViewText(R.id.textViewRowQuotation, quotationEntity.theQuotation());
        remoteViews.setTextViewText(R.id.textViewRowAuthor, quotationEntity.theAuthor());
        remoteViews.setTextViewText(R.id.textViewRowPosition, quotationPosition);

        synchronized (this) {
            if (!quotationList.isEmpty() && !"".equals(quotationEntity.theQuotation())) {
                remoteViews.setTextViewText(R.id.textViewRowQuotation, quotationEntity.theQuotation());
                remoteViews.setTextViewText(R.id.textViewRowAuthor, quotationEntity.theAuthor());
                remoteViews.setTextViewText(R.id.textViewRowPosition, quotationPosition);

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowQuotation,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowQuotation,
                        Color.parseColor(textColour));

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowAuthor,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowAuthor,
                        Color.parseColor(textColour));

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowPosition,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowPosition,
                        Color.parseColor(textColour));

                final int paintFlags = Paint.ANTI_ALIAS_FLAG;
                final String methodName = "setPaintFlags";

                if (!quotationEntity.wikipedia.equals("?")) {
                    remoteViews.setInt(R.id.textViewRowAuthor, methodName,
                            paintFlags | Paint.UNDERLINE_TEXT_FLAG);
                } else {
                    remoteViews.setInt(R.id.textViewRowQuotation, methodName, paintFlags);
                    remoteViews.setInt(R.id.textViewRowAuthor, methodName, paintFlags);
                    remoteViews.setInt(R.id.textViewRowPosition, methodName, paintFlags);
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
