package com.github.jameshnsears.quoteunquote.cloud.transfer

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget

object TransferUtility {
    fun getWidgetIds(context: Context): IntArray {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return appWidgetManager.getAppWidgetIds(
            ComponentName(
                context,
                QuoteUnquoteWidget::class.java,
            ),
        )
    }
}
