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
import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
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
    private final QuoteUnquoteWidget quoteUnquoteWidget;
    @Nullable
    public AppearancePreferences appearancePreferences;
    @Nullable
    public ContentPreferences contentPreferences;
    @Nullable
    private QuotationEntity quotationEntity;

    ListViewProvider(@NonNull final Context serviceContext, @NonNull final Intent intent) {
        this.context = serviceContext;
        this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        appearancePreferences = new AppearancePreferences(widgetId, serviceContext);
        contentPreferences = new ContentPreferences(widgetId, serviceContext);

        quoteUnquoteWidget = new QuoteUnquoteWidget();
    }

    @Override
    public void onCreate() {
        Timber.d("widgetId=%d", widgetId);
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("widgetId=%d", widgetId);

        synchronized (this) {
            quotationList.clear();

            quotationEntity = getQuoteUnquoteModel(context).getNext(
                    widgetId,
                    contentPreferences.getContentSelection());

            if (quotationEntity != null) {
                quotationList.add(quotationEntity.theQuotation());
            }
        }
    }

    @NonNull
    public QuoteUnquoteModel getQuoteUnquoteModel(@NonNull final Context listViewContext) {
        return new QuoteUnquoteModel(listViewContext);
    }

    @Override
    public void onDestroy() {
        Timber.d("widgetId=%d", widgetId);
    }

    @Override
    public int getCount() {
        synchronized (this) {
            return quotationList.size();
        }
    }

    @Override
    @NonNull
    public RemoteViews getViewAt(final int position) {
        final RemoteViews remoteViews = getRemoteViews(position);

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        remoteViews.setOnClickFillInIntent(android.R.id.text1, intent);

        return remoteViews;
    }

    @NonNull
    private RemoteViews getRemoteViews(final int position) {
        final RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(),
                android.R.layout.simple_list_item_1);

        synchronized (this) {
            if (!quotationList.isEmpty()) {

                remoteViews.setTextViewText(android.R.id.text1, quotationList.get(position));

                remoteViews.setTextViewTextSize(
                        android.R.id.text1,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.appearancePreferences.getAppearanceTextSize());

                remoteViews.setTextColor(
                        android.R.id.text1,
                        Color.parseColor(this.appearancePreferences.getAppearanceTextColour()));

                Timber.d("%d: digest=%s", widgetId, quotationEntity.digest);

                if (getQuoteUnquoteModel(context).isReported(widgetId)) {
                    remoteViews.setInt(android.R.id.text1, "setPaintFlags",
                            Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                } else {
                    remoteViews.setInt(android.R.id.text1, "setPaintFlags", Paint.ANTI_ALIAS_FLAG);
                }
            }
        }

        return remoteViews;
    }

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
