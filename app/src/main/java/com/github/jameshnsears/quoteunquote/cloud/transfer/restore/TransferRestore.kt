package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Appearance
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.cloud.transfer.Quotations
import com.github.jameshnsears.quoteunquote.cloud.transfer.Schedule
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferCommon
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferRestoreRequest
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.schedule.SchedulePreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.google.gson.GsonBuilder

class TransferRestore : TransferCommon() {
    fun requestJson(remoteCodeValue: String): String {
        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        return builder.create().toJson(TransferRestoreRequest(remoteCodeValue))
    }

    private fun emptySharedPreferencesExceptLocalCode(context: Context) {
        val preferencesFacade = PreferencesFacade(context)
        val localCode =
            preferencesFacade.preferenceHelper!!.getPreferenceString(preferencesFacade.localCode)
        PreferencesFacade.erase(context)
        preferencesFacade.preferenceHelper!!.setPreference(
            "0:CONTENT_FAVOURITES_LOCAL_CODE",
            localCode
        )
    }

    fun restore(context: Context, databaseRepository: DatabaseRepository, transfer: Transfer) {
        emptySharedPreferencesExceptLocalCode(context)
        databaseRepository.erase()

        restoreCurrent(context, databaseRepository, transfer.current)
        restoreFavourite(databaseRepository, transfer.favourites)
        restorePrevious(context, databaseRepository, transfer.previous)
        restoreSettings(context, transfer.settings)
    }

    private fun restoreCurrent(
        context: Context,
        databaseRepository: DatabaseRepository,
        currentList: List<Current>
    ) {
        /*
        restore 1 widget into 1 widget = current aligned

        restore 1 widget into 2 widgets = current duplicated

        restore 2 widgets into 1 widget = only first restore picked up
        */
        for (widgetId in TransferUtility.getWidgetIds(context)) {
            for (index in (currentList.size - 1) downTo 0 step 1) {
                val current = currentList[index]
                var digest = current.digest
                if (databaseRepository.getQuotation(current.digest) == null) {
                    digest = DatabaseRepository.getDefaultQuotationDigest()
                }

                databaseRepository.markAsCurrent(widgetId, digest)
            }
        }
    }

    private fun restoreFavourite(
        databaseRepository: DatabaseRepository,
        favouriteList: List<Favourite>
    ) {
        for (index in (favouriteList.size - 1) downTo 0 step 1) {
            val favourite = favouriteList[index]
            if (databaseRepository.getQuotation(favourite.digest) != null) {
                databaseRepository.markAsFavourite(favourite.digest)
            }
        }
    }

    fun restorePrevious(
        context: Context,
        databaseRepository: DatabaseRepository,
        previousList: List<Previous>
    ) {
        val favourites = 2
        val author = 3
        val search = 4

        for (widgetId in TransferUtility.getWidgetIds(context)) {
            for (index in (previousList.size - 1) downTo 0 step 1) {
                val previous = previousList[index]
                if (databaseRepository.getQuotation(previous.digest) != null) {
                    var contentSelection = ContentSelection.ALL
                    when (previous.contentType) {
                        favourites -> contentSelection = ContentSelection.FAVOURITES
                        author -> contentSelection = ContentSelection.AUTHOR
                        search -> contentSelection = ContentSelection.SEARCH
                    }
                    databaseRepository.markAsPrevious(widgetId, contentSelection, previous.digest)
                }
            }
        }
    }

    private fun restoreSettings(context: Context, settingsList: List<Settings>) {
        for (widgetId in TransferUtility.getWidgetIds(context)) {
            for (setting in settingsList) {
                restoreSettingsAppearance(widgetId, context, setting.appearance)
                restoreSettingsQuotations(widgetId, context, setting.quotations)
                restoreSettingsSchedules(widgetId, context, setting.schedule)
            }
        }
    }

    private fun restoreSettingsSchedules(
        widgetId: Int,
        context: Context,
        schedule: Schedule
    ) {
        val schedulePreferences = SchedulePreferences(widgetId, context)
        schedulePreferences.eventDaily = schedule.eventDaily
        schedulePreferences.eventDailyTimeHour = schedule.eventDailyHour
        schedulePreferences.eventDailyTimeMinute = schedule.eventDailyMinute
        schedulePreferences.eventDeviceUnlock = schedule.eventDeviceUnlock
        schedulePreferences.setEventdisplayWidgetAndNotification(schedule.eventDisplayAidgetAndNotification)
        schedulePreferences.eventDisplayWidget = schedule.eventDisplayWidget
        schedulePreferences.eventNextRandom = schedule.eventNextRandom
        schedulePreferences.eventNextSequential = schedule.eventNextSequential
    }

    private fun restoreSettingsQuotations(
        widgetId: Int,
        context: Context,
        quotations: Quotations
    ) {
        val quotationsPreferences = QuotationsPreferences(widgetId, context)
        quotationsPreferences.contentSelectionAuthor = quotations.contentAuthorName
        quotationsPreferences.contentSelectionSearchCount = quotations.contentSearchCount
        quotationsPreferences.contentAddToPreviousAll = quotations.contentAddToPreviousAll
        quotationsPreferences.contentSelectionSearch = quotations.contentSearchText

        if (quotations.contentAuthor) quotationsPreferences.contentSelection =
            ContentSelection.AUTHOR
        else if (quotations.contentFavourites) quotationsPreferences.contentSelection =
            ContentSelection.FAVOURITES
        else if (quotations.contentSearch) quotationsPreferences.contentSelection =
            ContentSelection.SEARCH
        else quotationsPreferences.contentSelection = ContentSelection.ALL
    }

    private fun restoreSettingsAppearance(
        widgetId: Int,
        context: Context,
        appearance: Appearance
    ) {
        val appearancePreferences = AppearancePreferences(widgetId, context)
        appearancePreferences.appearanceColour = appearance.appearanceColour
        appearancePreferences.appearanceTextColour = appearance.appearanceTextColour
        appearancePreferences.appearanceTextFamily = appearance.appearanceTextFamily
        appearancePreferences.appearanceTextSize = appearance.appearanceTextSize
        appearancePreferences.appearanceTextStyle = appearance.appearanceTextStyle
        appearancePreferences.appearanceToolbarColour = appearance.appearanceToolbarColour
        appearancePreferences.appearanceToolbarFavourite = appearance.appearanceToolbarFavourite
        appearancePreferences.appearanceToolbarFirst = appearance.appearanceToolbarFirst
        appearancePreferences.appearanceToolbarPrevious = appearance.appearanceToolbarPrevious
        appearancePreferences.appearanceToolbarRandom = appearance.appearanceToolbarRandom
        appearancePreferences.appearanceToolbarSequential = appearance.appearanceToolbarSequential
        appearancePreferences.appearanceToolbarShare = appearance.appearanceToolbarShare
        appearancePreferences.appearanceTransparency = appearance.appearanceTransparency
    }
}
