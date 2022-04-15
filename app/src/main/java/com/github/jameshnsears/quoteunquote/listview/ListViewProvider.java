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
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    private String quotationPosition;

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
                this.quotationList.add(this.quotationEntity.theQuotation());
                this.quotationList.add(this.quotationEntity.theAuthor());
                this.quotationList.add(this.quotationPosition);
            } else {
                // subsequent calls
                if (!"".equals(this.quotationEntity.theQuotation())
                        &&
                        !this.quotationList.get(0).equals(this.quotationEntity.theQuotation())) {
                    this.quotationList.set(0, this.quotationEntity.theQuotation());
                    this.quotationList.set(1, this.quotationEntity.theAuthor());
                    this.quotationList.set(2, this.quotationPosition);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        quoteUnquoteModel.databaseRepository.abstractHistoryDatabase = null;
        quoteUnquoteModel = null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    @NonNull
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = this.getRemoteViews(position);

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowQuotation,
                IntentFactoryHelper.createIntent(this.widgetId));

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowAuthor,
                IntentFactoryHelper.createClickFillInIntent(
                        "wikipedia",
                        this.quotationEntity.wikipedia,
                        widgetId));

        remoteViews.setOnClickFillInIntent(
                R.id.textViewRowPosition,
                IntentFactoryHelper.createIntent(this.widgetId));

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

        remoteViews.setTextViewText(R.id.textViewRowQuotation, this.quotationEntity.theQuotation());
        remoteViews.setTextViewText(R.id.textViewRowAuthor, this.quotationEntity.theAuthor());
        remoteViews.setTextViewText(R.id.textViewRowPosition, this.quotationPosition);

        synchronized (this) {
            if (!this.quotationList.isEmpty() && !"".equals(this.quotationEntity.theQuotation())) {
                remoteViews.setTextViewText(R.id.textViewRowQuotation, this.quotationEntity.theQuotation());
                remoteViews.setTextViewText(R.id.textViewRowAuthor, this.quotationEntity.theAuthor());
                remoteViews.setTextViewText(R.id.textViewRowPosition, this.quotationPosition);

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowQuotation,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowQuotation,
                        Color.parseColor(this.textColour));

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowAuthor,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowAuthor,
                        Color.parseColor(this.textColour));

                remoteViews.setTextViewTextSize(
                        R.id.textViewRowPosition,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.textSize);

                remoteViews.setTextColor(
                        R.id.textViewRowPosition,
                        Color.parseColor(this.textColour));

                final int paintFlags = Paint.ANTI_ALIAS_FLAG;
                final String methodName = "setPaintFlags";

                if (!this.quotationEntity.wikipedia.equals("?")) {
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
