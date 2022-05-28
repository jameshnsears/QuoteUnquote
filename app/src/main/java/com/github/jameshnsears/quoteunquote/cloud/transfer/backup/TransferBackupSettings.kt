package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Appearance
import com.github.jameshnsears.quoteunquote.cloud.transfer.Quotations
import com.github.jameshnsears.quoteunquote.cloud.transfer.Schedule
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import com.github.jameshnsears.quoteunquote.utils.ContentSelection

// Settings are set on demand in the UI; the settings listed here define all possible
// keys but not accurate values.
open class TransferBackupSettings(val context: Context) {
    fun settings(): List<Settings> {
        val settingsList = mutableListOf<Settings>()

        val widgetIds = TransferUtility.getWidgetIds(context)

        for (widgetIdsIndex in widgetIds.indices) {
            settingsList.add(
                Settings(
                    settingsQuotations(widgetIds[widgetIdsIndex], context),
                    settingsAppearance(widgetIds[widgetIdsIndex], context),
                    settingsSchedule(widgetIds[widgetIdsIndex], context),
                    widgetIds[widgetIdsIndex]
                )
            )
        }

        return settingsList
    }

    fun settingsQuotations(widgetId: Int, context: Context): Quotations {
        val quotationPreferences = QuotationsPreferences(widgetId, context)
        quotationPreferences.setContentSelection(quotationPreferences.contentSelection)

        var all = false
        var author = false
        var favourites = false
        var search = false
        when (quotationPreferences.contentSelection) {
            ContentSelection.ALL -> all = true
            ContentSelection.AUTHOR -> author = true
            ContentSelection.FAVOURITES -> favourites = true
            ContentSelection.SEARCH -> search = true
        }

        return Quotations(
            quotationPreferences.contentAddToPreviousAll,
            all,
            author,
            quotationPreferences.contentSelectionAuthor,
            favourites,
            search,
            quotationPreferences.contentSelectionSearchCount,
            quotationPreferences.contentSelectionSearch
        )
    }

    fun settingsAppearance(widgetId: Int, context: Context): Appearance {
        val appearancePreferences = AppearancePreferences(widgetId, context)

        return Appearance(
            appearancePreferences.appearanceTransparency,
            appearancePreferences.appearanceColour,
            appearancePreferences.appearanceTextFamily,
            appearancePreferences.appearanceTextStyle,
            appearancePreferences.appearanceTextSize,
            appearancePreferences.appearanceTextColour,
            appearancePreferences.appearanceToolbarColour,
            appearancePreferences.appearanceToolbarFirst,
            appearancePreferences.appearanceToolbarPrevious,
            appearancePreferences.appearanceToolbarFavourite,
            appearancePreferences.appearanceToolbarShare,
            appearancePreferences.appearanceToolbarRandom,
            appearancePreferences.appearanceToolbarSequential
        )
    }

    fun settingsSchedule(widgetId: Int, context: Context): Schedule {
        val notificationsPreferences =
            NotificationsPreferences(
                widgetId,
                context
            )

        return Schedule(
            notificationsPreferences.eventNextRandom,
            notificationsPreferences.eventNextSequential,
            notificationsPreferences.eventDisplayWidget,
            notificationsPreferences.eventDisplayWidgetAndNotification,
            notificationsPreferences.eventDaily,
            notificationsPreferences.eventDeviceUnlock,
            notificationsPreferences.eventDailyTimeMinute,
            notificationsPreferences.eventDailyTimeHour
        )
    }
}
