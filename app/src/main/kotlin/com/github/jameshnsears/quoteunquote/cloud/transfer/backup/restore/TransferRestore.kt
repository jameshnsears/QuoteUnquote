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

    fun restore(context: Context, databaseRepository: DatabaseRepository, transfer: Transfer) {
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

    private fun restoreCode(context: Context, restoreCode: String) {
        val preferencesFacade = PreferencesFacade(context)
        val localCode =
            preferencesFacade.preferenceHelper!!.getPreferenceString(preferencesFacade.localCode)

        Timber.d("localCode=%s; restoreCode=%s", localCode, restoreCode)

        preferencesFacade.preferenceHelper!!.setPreference(
            "0:CONTENT_FAVOURITES_LOCAL_CODE",
            restoreCode,
        )
    }

    private fun restoreFavourite(
        databaseRepository: DatabaseRepository,
        favouriteList: List<Favourite>,
    ) {
        val useInternalDatabase = DatabaseRepository.useInternalDatabase

        for (index in (favouriteList.size - 1) downTo 0 step 1) {
            val favourite = favouriteList[index]

            if (favourite.db == "internal" || favourite.db == null) {
                DatabaseRepository.useInternalDatabase = true
                databaseRepository.markAsFavourite(favourite.digest)
            } else {
                if (databaseRepository.countAllExternal().blockingGet() > 0) {
                    DatabaseRepository.useInternalDatabase = false
                    databaseRepository.markAsFavourite(favourite.digest)
                }
            }
        }

        DatabaseRepository.useInternalDatabase = useInternalDatabase
    }

    private fun restoreCurrent(
        context: Context,
        databaseRepository: DatabaseRepository,
        currentList: List<Current>,
    ) {
        val useInternalDatabase = DatabaseRepository.useInternalDatabase

        for (widgetId in TransferUtility.getWidgetIds(context)) {
            for (currentIndex in (currentList.size - 1) downTo 0 step 1) {
                val current = currentList[currentIndex]
                val digest = current.digest

                if (current.db == "internal" || current.db == null) {
                    DatabaseRepository.useInternalDatabase = true
                    restoreCurrentDigest(databaseRepository, widgetId, digest)
                } else {
                    if (databaseRepository.countAllExternal().blockingGet() > 0) {
                        DatabaseRepository.useInternalDatabase = false
                        restoreCurrentDigest(databaseRepository, widgetId, digest)
                    }
                }
            }
        }

        DatabaseRepository.useInternalDatabase = useInternalDatabase
    }

    private fun restoreCurrentDigest(
        databaseRepository: DatabaseRepository,
        widgetId: Int,
        digest: String,
    ) {
        val currentQuotation: QuotationEntity? =
            databaseRepository.getCurrentQuotation(widgetId)

        if (currentQuotation == null || currentQuotation.digest != digest) {
            databaseRepository.markAsCurrent(widgetId, digest)
        }
    }

    private fun restorePrevious(
        context: Context,
        databaseRepository: DatabaseRepository,
        previousList: List<Previous>,
    ) {
        val useInternalDatabase = DatabaseRepository.useInternalDatabase

        val favourites = ContentSelection.FAVOURITES.contentSelection
        val author = ContentSelection.AUTHOR.contentSelection
        val search = ContentSelection.SEARCH.contentSelection

        for (previousIndex in (previousList.size - 1) downTo 0 step 1) {
            for (widgetId in TransferUtility.getWidgetIds(context)) {
                val previous = previousList[previousIndex]

                var contentSelection = ContentSelection.ALL
                when (previous.contentType) {
                    favourites -> contentSelection = ContentSelection.FAVOURITES
                    author -> contentSelection = ContentSelection.AUTHOR
                    search -> contentSelection = ContentSelection.SEARCH
                }

                if (previous.db == "internal" || previous.db == null) {
                    DatabaseRepository.useInternalDatabase = true
                    restorePreviousDigest(databaseRepository, widgetId, contentSelection, previous)
                } else {
                    if (databaseRepository.countAllExternal().blockingGet() > 0) {
                        DatabaseRepository.useInternalDatabase = false
                        restorePreviousDigest(
                            databaseRepository,
                            widgetId,
                            contentSelection,
                            previous,
                        )
                    }
                }
            }
        }

        DatabaseRepository.useInternalDatabase = useInternalDatabase
    }

    private fun restorePreviousDigest(
        databaseRepository: DatabaseRepository,
        widgetId: Int,
        contentSelection: ContentSelection,
        previous: Previous,
    ) {
        if (databaseRepository.countPreviousDigest(
                widgetId,
                contentSelection,
                previous.digest,
            ) == 0
        ) {
            databaseRepository.markAsPrevious(
                widgetId,
                contentSelection,
                previous.digest,
            )
        } else {
            Timber.d("digest already in previous: digest=%s", previous.digest)
        }
    }

    private fun restoreSettings(context: Context, settingsList: List<Settings>) {
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
