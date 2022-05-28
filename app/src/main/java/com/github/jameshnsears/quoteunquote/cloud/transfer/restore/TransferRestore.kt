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
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.google.gson.GsonBuilder
import timber.log.Timber

class TransferRestore : TransferCommon() {
    fun requestJson(remoteCodeValue: String): String {
        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        return builder.create().toJson(TransferRestoreRequest(remoteCodeValue))
    }

    private fun emptySharedPreferencesExceptArchive(context: Context) {
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
        emptySharedPreferencesExceptArchive(context)
        databaseRepository.eraseForRestore()

        restoreFavourite(databaseRepository, transfer.favourites)

        /*
        restore 1 widget into 1 widget -> widgets identical
        restore 1 widget into 2 widgets -> both widgets same as 1

        restore 2 widgets into 1 widget
            -> widget same as 1 except all unique previous from 1 and 2 also in 1
        restore 2 widgets into 2 widgets
            -> widgets identical except all unique previous from 1 and 2 also in both
        */

        restoreCurrent(context, databaseRepository, transfer.current)
        restorePrevious(context, databaseRepository, transfer.previous)
        restoreSettings(context, transfer.settings)
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

    private fun restoreCurrent(
        context: Context,
        databaseRepository: DatabaseRepository,
        currentList: List<Current>
    ) {
        for (widgetId in TransferUtility.getWidgetIds(context)) {

            for (currentIndex in (currentList.size - 1) downTo 0 step 1) {
                val current = currentList[currentIndex]
                var digest = current.digest

                if (databaseRepository.getQuotation(current.digest) == null) {
                    digest = DatabaseRepository.getDefaultQuotationDigest()
                }

                val currentQuotation: QuotationEntity? = databaseRepository.getCurrentQuotation(widgetId)
                if (currentQuotation == null || currentQuotation.digest != digest) {
                    databaseRepository.markAsCurrent(widgetId, digest)
                }
            }
        }
    }

    private fun restorePrevious(
        context: Context,
        databaseRepository: DatabaseRepository,
        previousList: List<Previous>
    ) {
        val favourites = ContentSelection.FAVOURITES.contentSelection
        val author = ContentSelection.AUTHOR.contentSelection
        val search = ContentSelection.SEARCH.contentSelection

        for (previousIndex in (previousList.size - 1) downTo 0 step 1) {
            for (widgetId in TransferUtility.getWidgetIds(context)) {
                val previous = previousList[previousIndex]

                if (databaseRepository.getQuotation(previous.digest) != null) {
                    var contentSelection = ContentSelection.ALL
                    when (previous.contentType) {
                        favourites -> contentSelection = ContentSelection.FAVOURITES
                        author -> contentSelection = ContentSelection.AUTHOR
                        search -> contentSelection = ContentSelection.SEARCH
                    }

                    if (databaseRepository.countPreviousDigest(
                            widgetId,
                            contentSelection,
                            previous.digest
                        ) == 0
                    ) {
                        databaseRepository.markAsPrevious(
                            widgetId,
                            contentSelection,
                            previous.digest
                        )
                    } else {
                        Timber.d("digest already present: digest=%s", previous.digest)
                    }
                } else {
                    Timber.d("unknown digest: digest=%s", previous.digest)
                }
            }
        }
    }

    private fun restoreSettings(context: Context, settingsList: List<Settings>) {
        var settingsListIndex = 0
        for (widgetId in TransferUtility.getWidgetIds(context)) {
            val settings = settingsList[settingsListIndex]

            restoreSettingsAppearance(
                widgetId,
                context,
                settings.appearance
            )
            restoreSettingsQuotations(
                widgetId,
                context,
                settings.quotations
            )
            restoreSettingsSchedules(
                widgetId,
                context,
                settings.schedule
            )

            // move to next settingsList if it's available, else reuse last one
            if (settingsListIndex < settingsList.size - 1) {
                settingsListIndex++
            }
        }
    }

    private fun restoreSettingsSchedules(
        widgetId: Int,
        context: Context,
        schedule: Schedule
    ) {
        val notificationsPreferences =
            NotificationsPreferences(
                widgetId,
                context
            )
        notificationsPreferences.eventDaily = schedule.eventDaily
        notificationsPreferences.eventDailyTimeHour = schedule.eventDailyHour
        notificationsPreferences.eventDailyTimeMinute = schedule.eventDailyMinute
        notificationsPreferences.eventDeviceUnlock = schedule.eventDeviceUnlock
        notificationsPreferences.setEventdisplayWidgetAndNotification(schedule.eventDisplayAidgetAndNotification)
        notificationsPreferences.eventDisplayWidget = schedule.eventDisplayWidget
        notificationsPreferences.eventNextRandom = schedule.eventNextRandom
        notificationsPreferences.eventNextSequential = schedule.eventNextSequential
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
