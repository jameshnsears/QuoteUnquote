package com.github.jameshnsears.quoteunquote.listview;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences;
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
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    private QuotationEntity quotationEntity = null;
    @Nullable
    private String postion = "";

    ListViewProvider(@NonNull Context context, @NonNull Intent intent) {
        synchronized (this) {
            this.context = context;

            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

            setQuoteUnquoteModel(new QuoteUnquoteModel(widgetId, context));

            QuotationEntity currentQuotation = getQuoteUnquoteModel().getCurrentQuotation(widgetId);

            if (currentQuotation != null) {
                quotationEntity = getQuoteUnquoteModel().getCurrentQuotation(widgetId);

                postion = getQuoteUnquoteModel().getPosition(widgetId, quotationEntity.digest);
            } else {
                Timber.w("currentQuotation==null");
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
        synchronized (this) {
            if (quotationList.isEmpty() && quotationEntity != null) {
                // first time call
                quotationList.add(quotationEntity.theQuotation());
                quotationList.add(quotationEntity.theAuthor());
                quotationList.add(postion);
            } else {
                // subsequent calls
                if (quotationEntity != null) {
                    try {
                        if (!"".equals(quotationEntity.theQuotation())
                                && !quotationList.get(0).equals(quotationEntity.theQuotation())) {
                            quotationList.set(0, quotationEntity.theQuotation());
                            quotationList.set(1, quotationEntity.theAuthor());
                            quotationList.set(2, postion);
                        }
                    } catch (NullPointerException e) {
                        Timber.e(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        // ...
    }

    @Override
    @NonNull
    public RemoteViews getViewAt(final int position) {
        final RemoteViews remoteViews = getRemoteViews(position);

        if (quotationEntity != null) {
            try {
                remoteViews.setOnClickFillInIntent(
                        R.id.textViewRowQuotation,
                        IntentFactoryHelper.createIntent(widgetId));

                if (quotationEntity.wikipedia != null) {
                    remoteViews.setOnClickFillInIntent(
                            R.id.textViewRowAuthor,
                            IntentFactoryHelper.createClickFillInIntent(
                                    "wikipedia",
                                    quotationEntity.wikipedia,
                                    widgetId));
                }

                remoteViews.setOnClickFillInIntent(
                        R.id.textViewRowPosition,
                        IntentFactoryHelper.createIntent(widgetId));
            } catch (NullPointerException e) {
                Timber.e("%s", e.getMessage());
            }
        }

        return remoteViews;
    }

    private int getRowLayoutId(
            String textFamily,
            String textStyle,
            boolean forceItalicRegular,
            boolean center) {
        int layoutId = 0;

        switch (textFamily) {
            case "Cursive":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForCursive(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_0_cursive_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_0_cursive_forced_center;
                }
                break;

            case "Monospace":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForMonospace(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_1_monospace_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_1_monospace_forced_center;
                }
                break;

            case "Sans Serif":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerif(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_2_sans_serif_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_2_sans_serif_forced_center;
                }
                break;

            case "Sans Serif Condensed":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerifCondensed(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_3_sans_serif_condensed_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_3_sans_serif_condensed_forced_center;
                }
                break;

            case "Sans Serif Medium":
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSansSerifMedium(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_4_sans_serif_medium_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_4_sans_serif_medium_forced_center;
                }
                break;

            default:
                layoutId = ListViewLayoutIdHelper.Companion.layoutIdForSerif(textStyle, center);
                if (forceItalicRegular && !center) {
                    layoutId = R.layout.listvew_row_5_serif_forced;
                }
                if (forceItalicRegular && center) {
                    layoutId = R.layout.listvew_row_5_serif_forced_center;
                }
                break;
        }

        return layoutId;
    }

    @NonNull
    private RemoteViews getRemoteViews(final int position) {
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        final RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                getRowLayoutId(
                        appearancePreferences.getAppearanceTextFamily(),
                        appearancePreferences.getAppearanceTextStyle(),
                        appearancePreferences.getAppearanceTextForceItalicRegular(),
                        appearancePreferences.getAppearanceTextCenter()
                )
        );

        if (!quotationList.isEmpty()
                && !"".equals(quotationEntity.theQuotation())
                && !"".equals(postion)) {
            setRemoteViewQuotation(remoteViews);
            setRemoteViewAuthor(remoteViews);
            setRemoteViewPosition(remoteViews);
        }

        return remoteViews;
    }

    private void setRemoteViewQuotation(RemoteViews remoteViews) {
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        setText(remoteViews, R.id.textViewRowQuotation, quotationEntity.theQuotation());
        setTextSize(remoteViews, R.id.textViewRowQuotation, appearancePreferences.getAppearanceQuotationTextSize());
        setTextColour(remoteViews, R.id.textViewRowQuotation, appearancePreferences.getAppearanceQuotationTextColour());
        setTextPaintFlags(remoteViews, R.id.textViewRowQuotation, Paint.ANTI_ALIAS_FLAG);
    }

    private void setRemoteViewHide(RemoteViews remoteViews, boolean hideView, int id) {
        if (hideView) {
            remoteViews.setViewVisibility(id, View.GONE);
        } else {
            remoteViews.setViewVisibility(id, View.VISIBLE);
        }
    }

    private void setRemoteViewAuthor(RemoteViews remoteViews) {
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        setText(remoteViews, R.id.textViewRowAuthor, quotationEntity.theAuthor());
        setTextSize(remoteViews, R.id.textViewRowAuthor, appearancePreferences.getAppearanceAuthorTextSize());
        setTextColour(remoteViews, R.id.textViewRowAuthor, appearancePreferences.getAppearanceAuthorTextColour());
        if (!quotationEntity.wikipedia.equals("?")) {
            setTextPaintFlags(
                    remoteViews,
                    R.id.textViewRowAuthor,
                    Paint.ANTI_ALIAS_FLAG | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            setTextPaintFlags(remoteViews, R.id.textViewRowAuthor, Paint.ANTI_ALIAS_FLAG);
        }
        setRemoteViewHide(
                remoteViews,
                appearancePreferences.getAppearanceAuthorTextHide(),
                R.id.textViewRowAuthor);
    }

    private void setRemoteViewPosition(RemoteViews remoteViews) {
        AppearancePreferences appearancePreferences = new AppearancePreferences(widgetId, context);

        setText(remoteViews, R.id.textViewRowPosition, postion);
        setTextSize(remoteViews, R.id.textViewRowPosition, appearancePreferences.getAppearancePositionTextSize());
        setTextColour(remoteViews, R.id.textViewRowPosition, appearancePreferences.getAppearancePositionTextColour());
        setTextPaintFlags(remoteViews, R.id.textViewRowPosition, Paint.ANTI_ALIAS_FLAG);
        setRemoteViewHide(
                remoteViews,
                appearancePreferences.getAppearancePositionTextHide(),
                R.id.textViewRowPosition);
    }

    private void setTextPaintFlags(RemoteViews remoteViews, int viewId, int paintFlags) {
        remoteViews.setInt(viewId, "setPaintFlags", paintFlags);
    }

    private void setText(RemoteViews remoteViews, int viewId, String text) {
        remoteViews.setTextViewText(viewId, text);
    }

    private void setTextColour(RemoteViews remoteViews, int viewId, String colour) {
        AppearancePreferences appearancePreferences = new AppearancePreferences(context);
        if (appearancePreferences.getAppearanceForceFollowSystemTheme()) {
            if (isNightMode(context)) {
                remoteViews.setTextColor(viewId, Color.WHITE);
            } else {
                remoteViews.setTextColor(viewId, Color.BLACK);
            }
        } else {
            remoteViews.setTextColor(
                    viewId,
                    Color.parseColor(colour));
        }
    }

    private boolean isNightMode(@NonNull Context context) {
        boolean isNightMode = false;

        if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true;
        }

        return isNightMode;
    }

    private void setTextSize(RemoteViews remoteViews, int viewId, int size) {
        remoteViews.setTextViewTextSize(
                viewId,
                TypedValue.COMPLEX_UNIT_DIP,
                (float) size);
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

    @Override
    public int getCount() {
        return 1;
    }
}
