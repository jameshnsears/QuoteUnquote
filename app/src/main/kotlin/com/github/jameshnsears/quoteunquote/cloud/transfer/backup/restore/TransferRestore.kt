package com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.github.jameshnsears.quoteunquote.cloud.transfer.Appearance
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.cloud.transfer.Quotations
import com.github.jameshnsears.quoteunquote.cloud.transfer.Schedule
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.Sync
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferCommon
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferRestoreRequest
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.h.CurrentEntity
import com.github.jameshnsears.quoteunquote.db.h.FavouriteEntity
import com.github.jameshnsears.quoteunquote.db.h.PreviousEntity
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

    private fun emptySharedPreferencesExceptLocalCode(context: Context) {
        val preferencesFacade = PreferencesFacade(context)
        val localCode =
            preferencesFacade.preferenceHelper!!.getPreferenceString(preferencesFacade.localCode)

        PreferencesFacade.erase(context)
        preferencesFacade.preferenceHelper!!.setPreference(
            "0:CONTENT_FAVOURITES_LOCAL_CODE",
            localCode,
        )
    }

    fun newDeviceTransferTransformer(transfer: Transfer): Transfer {
        val current = transfer.current.takeLast(1)

        return Transfer(
            transfer.code,
            current,
            transfer.favourites,
            onlyKeepPreviousWithWidgetId(transfer.previous, current[0].widgetId),
            transfer.settings.takeLast(1),
        )
    }

    private fun onlyKeepPreviousWithWidgetId(
        previous: List<Previous>,
        widgetId: Int,
    ): List<Previous> {
        val filteredPrevious = mutableListOf<Previous>()

        previous.forEach { i ->
            if (i.widgetId == widgetId && !filteredPrevious.contains(i)) {
                filteredPrevious.add(i)
            }
        }

        return filteredPrevious
    }

    fun restorePurge(
        context: Context,
        databaseRepository: DatabaseRepository,
        transfer: Transfer,
    ) {
        Timber.d("restorePurge")

        restore(
            context,
            databaseRepository,
            newDeviceTransferTransformer(transfer),
        )
    }

    fun restore(
        context: Context,
        databaseRepository: DatabaseRepository,
        transfer: Transfer,
    ) {
        Timber.d("restore")

        emptySharedPreferencesExceptLocalCode(context)
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
        restoreCode(context, transfer.code)
    }

    private fun restoreCode(
        context: Context,
        restoreCode: String,
    ) {
        val preferencesFacade = PreferencesFacade(context)
        val localCode =
            preferencesFacade.preferenceHelper!!.getPreferenceString(preferencesFacade.localCode)

        Timber.d("localCode=%s; restoreCode=%s", localCode, restoreCode)

        preferencesFacade.preferenceHelper!!.setPreference(
            "0:CONTENT_FAVOURITES_LOCAL_CODE",
            restoreCode,
        )
    }

    internal fun restoreFavourite(
        databaseRepository: DatabaseRepository,
        favouriteList: List<Favourite>,
    ) {
        val internalFavourites = mutableListOf<FavouriteEntity>()
        val externalFavourites = mutableListOf<FavouriteEntity>()
        val externalHasQuotations = databaseRepository.countAllExternal().blockingGet() > 0

        val seenInternalDigests = mutableSetOf<String>()
        val seenExternalDigests = mutableSetOf<String>()

        for (favourite in favouriteList.reversed()) {
            val digest = favourite.digest
            if (favourite.db == "internal" || favourite.db == null) {
                if (!seenInternalDigests.add(digest)) continue
                if (databaseRepository.getQuotation(true, digest) != null) {
                    internalFavourites.add(FavouriteEntity(digest))
                }
            } else if (externalHasQuotations) {
                if (!seenExternalDigests.add(digest)) continue
                if (databaseRepository.getQuotation(false, digest) != null) {
                    externalFavourites.add(FavouriteEntity(digest))
                }
            }
        }

        if (internalFavourites.isNotEmpty()) {
            databaseRepository.insertFavourites(internalFavourites, true)
        }
        if (externalFavourites.isNotEmpty()) {
            databaseRepository.insertFavourites(externalFavourites, false)
        }
    }

    private fun restoreCurrent(
        context: Context,
        databaseRepository: DatabaseRepository,
        currentList: List<Current>,
    ) {
        val internalCurrent = mutableListOf<CurrentEntity>()
        val externalCurrent = mutableListOf<CurrentEntity>()
        val externalHasQuotations = databaseRepository.countAllExternal().blockingGet() > 0

        val widgetIds = TransferUtility.getWidgetIds(context)
        for (widgetId in widgetIds) {
            for (current in currentList.reversed()) {
                val entity = CurrentEntity(widgetId, current.digest)
                if (current.db == "internal" || current.db == null) {
                    internalCurrent.add(entity)
                } else if (externalHasQuotations) {
                    externalCurrent.add(entity)
                }
            }
        }

        if (internalCurrent.isNotEmpty()) {
            databaseRepository.insertCurrent(internalCurrent, true)
        }
        if (externalCurrent.isNotEmpty()) {
            databaseRepository.insertCurrent(externalCurrent, false)
        }
    }

    internal fun restorePrevious(
        context: Context,
        databaseRepository: DatabaseRepository,
        previousList: List<Previous>,
    ) {
        val internalPrevious = mutableListOf<PreviousEntity>()
        val externalPrevious = mutableListOf<PreviousEntity>()
        val externalHasQuotations = databaseRepository.countAllExternal().blockingGet() > 0

        val favourites = ContentSelection.FAVOURITES.contentSelection
        val author = ContentSelection.AUTHOR.contentSelection
        val search = ContentSelection.SEARCH.contentSelection

        val seenKey = mutableSetOf<String>()

        val widgetIds = TransferUtility.getWidgetIds(context)
        for (previous in previousList.reversed()) {
            val contentSelection =
                when (previous.contentType) {
                    favourites -> ContentSelection.FAVOURITES
                    author -> ContentSelection.AUTHOR
                    search -> ContentSelection.SEARCH
                    else -> ContentSelection.ALL
                }

            for (widgetId in widgetIds) {
                val entity = PreviousEntity(widgetId, contentSelection, previous.digest)

                // dedup across multiple source items that resolve to the same
                // (widgetId, contentSelection, digest) triple
                if (!seenKey.add("$widgetId|${contentSelection.contentSelection}|${previous.digest}")) {
                    continue
                }

                if (previous.db == "internal" || previous.db == null) {
                    internalPrevious.add(entity)
                } else if (externalHasQuotations) {
                    externalPrevious.add(entity)
                }
            }
        }

        if (internalPrevious.isNotEmpty()) {
            databaseRepository.insertPrevious(internalPrevious, true)
        }
        if (externalPrevious.isNotEmpty()) {
            databaseRepository.insertPrevious(externalPrevious, false)
        }
    }

    private fun restoreSettings(
        context: Context,
        settingsList: List<Settings>,
    ) {
        var settingsListIndex = 0
        for (widgetId in TransferUtility.getWidgetIds(context)) {
            val settings = settingsList[settingsListIndex]

            restoreSettingsAppearance(
                widgetId,
                context,
                settings.appearance,
            )
            restoreSettingsQuotations(
                widgetId,
                context,
                settings.quotations,
            )
            restoreSettingsSchedules(
                widgetId,
                context,
                settings.schedule,
            )
            restoreSettingsSync(
                widgetId,
                context,
                settings.sync,
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
        schedule: Schedule,
    ) {
        val notificationsPreferences =
            NotificationsPreferences(
                widgetId,
                context,
            )

        notificationsPreferences.eventTtsUk = schedule.eventEventTtsUk
        notificationsPreferences.eventTtsSystem = schedule.eventEventTtsSystem

        notificationsPreferences.eventDaily = schedule.eventDaily
        notificationsPreferences.eventDailyTimeHour = schedule.eventDailyHour
        notificationsPreferences.eventDailyTimeMinute = schedule.eventDailyMinute

        notificationsPreferences.customisableInterval = schedule.eventEventCustomisableInterval

        Timber.d("from = %d", schedule.eventEventCustomisableIntervalHourFrom)
        notificationsPreferences.customisableIntervalHourFrom =
            schedule.eventEventCustomisableIntervalHourFrom

        Timber.d("to = %d", schedule.eventEventCustomisableIntervalHourTo)
        notificationsPreferences.customisableIntervalHourTo =
            schedule.eventEventCustomisableIntervalHourTo

        Timber.d("hours = %d", schedule.eventEventCustomisableIntervalHours)
        notificationsPreferences.customisableIntervalHours =
            schedule.eventEventCustomisableIntervalHours

        notificationsPreferences.eventDisplayWidgetAndNotification =
            schedule.eventDisplayWidgetAndNotification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alarmManager: AlarmManager = context.getSystemService<AlarmManager>()!!
            if (!alarmManager.canScheduleExactAlarms()) {
                notificationsPreferences.eventDaily = false
                notificationsPreferences.customisableInterval = false
            }

            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                notificationsPreferences.eventDisplayWidgetAndNotification = false
            }
        }

        notificationsPreferences.eventDeviceUnlock = schedule.eventDeviceUnlock
        notificationsPreferences.excludeSourceFromNotification =
            schedule.eventExcludeSourceFromNotification

        notificationsPreferences.eventDisplayWidget = schedule.eventDisplayWidget
        if (!notificationsPreferences.eventDisplayWidgetAndNotification) {
            notificationsPreferences.eventDisplayWidget = true
        }

        notificationsPreferences.eventNextRandom = schedule.eventNextRandom
        notificationsPreferences.eventNextSequential = schedule.eventNextSequential
    }

    private fun restoreSettingsQuotations(
        widgetId: Int,
        context: Context,
        quotations: Quotations,
    ) {
        val quotationsPreferences = QuotationsPreferences(widgetId, context)
        quotationsPreferences.contentSelectionAllExclusion = quotations.contentAllExclusion
        quotationsPreferences.contentAddToPreviousAll = quotations.contentAddToPreviousAll

        quotationsPreferences.contentSelectionAuthorCount = quotations.contentAuthorNameCount
        quotationsPreferences.contentSelectionAuthor = quotations.contentAuthorName

        quotationsPreferences.contentSelectionSearch = quotations.contentSearchText
        quotationsPreferences.contentSelectionSearchCount = quotations.contentSearchCount

        if (quotations.contentAuthor) {
            quotationsPreferences.contentSelection =
                ContentSelection.AUTHOR
        } else if (quotations.contentFavourites) {
            quotationsPreferences.contentSelection =
                ContentSelection.FAVOURITES
        } else if (quotations.contentSearch) {
            quotationsPreferences.contentSelection =
                ContentSelection.SEARCH
        } else {
            quotationsPreferences.contentSelection = ContentSelection.ALL
        }

        // we always move back to the Internal after a restore
        quotationsPreferences.databaseInternal = true
        quotationsPreferences.databaseExternalCsv = false
        quotationsPreferences.databaseExternalWeb = false
        quotationsPreferences.databaseWebUrl = quotations.databaseWebUrl
        quotationsPreferences.databaseWebXpathQuotation = quotations.databaseWebXpathQuotation
        quotationsPreferences.databaseWebXpathSource = quotations.databaseWebXpathSource
        quotationsPreferences.databaseWebKeepLatestOnly = quotations.databaseWebKeepLatestOnly
    }

    private fun restoreSettingsAppearance(
        widgetId: Int,
        context: Context,
        appearance: Appearance,
    ) {
        val appearancePreferences = AppearancePreferences(widgetId, context)
        appearancePreferences.appearanceColour = appearance.appearanceColour
        appearancePreferences.appearanceTransparency = appearance.appearanceTransparency
        appearancePreferences.appearanceTextFamily = appearance.appearanceTextFamily
        appearancePreferences.appearanceTextStyle = appearance.appearanceTextStyle
        appearancePreferences.appearanceTextForceItalicRegular =
            appearance.appearanceTextForceItalicRegular
        appearancePreferences.appearanceQuotationTextColour = appearance.appearanceTextColour
        appearancePreferences.appearanceQuotationTextSize = appearance.appearanceTextSize
        appearancePreferences.appearanceAuthorTextColour = appearance.appearanceAuthorTextColour
        appearancePreferences.appearanceAuthorTextSize = appearance.appearanceAuthorTextSize
        appearancePreferences.appearanceAuthorTextHide = appearance.appearanceAuthorTextHide
        appearancePreferences.appearancePositionTextColour = appearance.appearancePositionTextColour
        appearancePreferences.appearancePositionTextSize = appearance.appearancePositionTextSize
        appearancePreferences.appearancePositionTextHide = appearance.appearancePositionTextHide
        appearancePreferences.appearanceForceFollowSystemTheme =
            appearance.appearanceForceFollowSystemTheme
        appearancePreferences.appearanceToolbarColour = appearance.appearanceToolbarColour
        appearancePreferences.appearanceToolbarFavourite = appearance.appearanceToolbarFavourite
        appearancePreferences.appearanceToolbarFirst = appearance.appearanceToolbarFirst
        appearancePreferences.appearanceToolbarPrevious = appearance.appearanceToolbarPrevious
        appearancePreferences.appearanceToolbarRandom = appearance.appearanceToolbarRandom
        appearancePreferences.appearanceToolbarSequential = appearance.appearanceToolbarSequential
        appearancePreferences.appearanceToolbarShare = appearance.appearanceToolbarShare
        appearancePreferences.appearanceToolbarShareNoSource =
            appearance.appearanceToolbarShareNoSource
        appearancePreferences.appearanceToolbarJump = appearance.appearanceToolbarJump
        appearancePreferences.appearanceToolbarPosition = appearance.appearanceToolbarPosition
    }

    private fun restoreSettingsSync(
        widgetId: Int,
        context: Context,
        sync: Sync,
    ) {
        val syncPreferences = SyncPreferences(widgetId, context)
        if (sync != null && sync.syncAutoCloudBackup != null) {
            syncPreferences.autoCloudBackup = sync.syncAutoCloudBackup
        }
    }
}
