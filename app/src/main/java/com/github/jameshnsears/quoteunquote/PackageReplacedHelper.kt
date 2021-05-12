package com.github.jameshnsears.quoteunquote

import android.content.Context
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventPreferences

class PackageReplacedHelper(val widgetId: Int, val context: Context) {
    fun alignHistoryWithQuotations(quoteUnquoteModel: QuoteUnquoteModel) {
        quoteUnquoteModel.alignHistoryWithQuotations(widgetId)
        quoteUnquoteModel.alignFavouritesWithQuotations(widgetId)
        quoteUnquoteModel.markAsCurrentDefault(widgetId)
    }

    fun migratePreferences() {
        AppearancePreferences(widgetId, context).performMigration()
        ContentPreferences(widgetId, context).performMigration()
        EventPreferences(widgetId, context).performMigration()
    }
}
