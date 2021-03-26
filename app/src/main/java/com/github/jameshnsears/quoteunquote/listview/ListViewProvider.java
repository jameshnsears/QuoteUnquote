package com.github.jameshnsears.quoteunquote.listview;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

class ListViewProvider implements RemoteViewsService.RemoteViewsFactory {
    private static final String LOG_TAG = ListViewProvider.class.getSimpleName();

    private final List<String> quotationList = new ArrayList<>();
    private final Context context;
    public Preferences preferences;
    private int widgetId;
    private QuotationEntity quotationEntity;
    private QuoteUnquoteWidget quoteUnquoteWidget;

    public ListViewProvider(final Context context, final Intent intent) {
        Log.d(LOG_TAG, "ListViewProvider");
        this.context = context;
        this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        preferences = new Preferences(widgetId, context);
        quoteUnquoteWidget = new QuoteUnquoteWidget();
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));
    }

    @Override
    public void onDataSetChanged() {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        synchronized (this) {
            quotationList.clear();

            quotationEntity = getQuoteUnquoteModel(context).getNext(
                    widgetId,
                    preferences.getSelectedContentType());

            if (quotationEntity != null) {
                quotationList.add(quotationEntity.theQuotation());
            }
        }
    }

    public QuoteUnquoteModel getQuoteUnquoteModel(final Context context) {
        return quoteUnquoteWidget.getQuoteUnquoteModelInstance(context);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));
    }

    @Override
    public int getCount() {
        synchronized (this) {
            return quotationList.size();
        }
    }

    @Override
    public RemoteViews getViewAt(final int position) {
        final RemoteViews remoteViews = getRemoteViews(position);

        final Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        remoteViews.setOnClickFillInIntent(android.R.id.text1, intent);

        return remoteViews;
    }

    private RemoteViews getRemoteViews(final int position) {
        final RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(),
                android.R.layout.simple_list_item_1);

        synchronized (this) {
            if (!quotationList.isEmpty()) {

                remoteViews.setTextViewText(android.R.id.text1, quotationList.get(position));

                remoteViews.setTextViewTextSize(
                        android.R.id.text1,
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) this.preferences.getSharedPreferenceInt(Preferences.FRAGMENT_APPEARANCE, Preferences.SPINNER_SIZE));

                remoteViews.setTextColor(
                        android.R.id.text1,
                        Color.parseColor(this.preferences.getSharedPreferenceString(
                                Preferences.FRAGMENT_APPEARANCE, Preferences.SPINNER_COLOUR)));

                Log.d(LOG_TAG, String.format("%d: %s: digest=%s", widgetId,
                        new Object() {
                        }.getClass().getEnclosingMethod().getName(),
                        quotationEntity.digest));

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
