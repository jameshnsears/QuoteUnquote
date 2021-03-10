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
    public AppearancePreferences appearancePreferences;
    @Nullable
    public ContentPreferences contentPreferences;

    ListViewProvider(@NonNull final Context context, @NonNull final Intent intent) {
        this.context = context;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        appearancePreferences = new AppearancePreferences(widgetId, context);
        contentPreferences = new ContentPreferences(widgetId, context);
    }

    @Override
    public void onCreate() {
        // ...
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("widgetId=%d", widgetId);

        synchronized (this) {
            quotationList.clear();

            QuotationEntity quotationEntity = getQuoteUnquoteModel().getNextQuotation(
                    widgetId,
                    contentPreferences.getContentSelection());

            if (quotationEntity != null) {
                quotationList.add(quotationEntity.theQuotation());
            }
        }
    }

    @NonNull
    public QuoteUnquoteModel getQuoteUnquoteModel() {
        return new QuoteUnquoteModel(context);
    }

    @Override
    public void onDestroy() {
        // ...
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
        Timber.d("getviewAt=%d", position);

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        final RemoteViews remoteViews = getRemoteViews(position);
        remoteViews.setOnClickFillInIntent(android.R.id.text1, intent);

        return remoteViews;
    }

    @NonNull
    private RemoteViews getRemoteViews(final int position) {
        Timber.d("getRemoteViews=%d", position);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                android.R.layout.simple_list_item_1);

        synchronized (this) {
            if (!quotationList.isEmpty()) {
                remoteViews.setTextViewText(android.R.id.text1, quotationList.get(position));

                if (appearancePreferences.getAppearanceTextSize() != -1) {
                    remoteViews.setTextViewTextSize(
                            android.R.id.text1,
                            TypedValue.COMPLEX_UNIT_DIP,
                            (float) appearancePreferences.getAppearanceTextSize());
                }

                if (!appearancePreferences.getAppearanceTextColour().equals("")) {
                    remoteViews.setTextColor(
                            android.R.id.text1,
                            Color.parseColor(appearancePreferences.getAppearanceTextColour()));
                }

                int paintFlags = Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG;

                if (getQuoteUnquoteModel().isReported(widgetId)) {
                    remoteViews.setInt(android.R.id.text1, "setPaintFlags",
                            paintFlags | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    remoteViews.setInt(android.R.id.text1, "setPaintFlags", paintFlags);
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
