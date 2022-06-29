package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreExternalTest : TransferRestoreUtility() {
    @Test
    fun restoreWhereExternalBeenPopulated() {
        if (canWorkWithMockk()) {
            // set up
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(1)

            createSharedPreferencesWithLocalCode(getLocalCode())

            insertInternalQuotations()
            insertExternalQuotations()

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget_with_external.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)

//            val backupJson = TransferBackup(context).asJson(backupTransfer)
            /*
{
  "code": "F9aT8HEW6d",
  "current": [
    {
      "db": "internal",
      "digest": "d1234567",
      "widget_id": 1
    },
    {
      "db": "external",
      "digest": "00000000",
      "widget_id": 1
    }
  ],
  "favourite": [
    {
      "db": "internal",
      "digest": "d5678901",
      "navigation": 1
    },
    {
      "db": "external",
      "digest": "00000000",
      "navigation": 1
    }
  ],
  "previous": [
    {
      "content_type": 1,
      "db": "internal",
      "digest": "d1234567",
      "navigation": 1,
      "widget_id": 1
    },
    {
      "content_type": 1,
      "db": "external",
      "digest": "00000000",
      "navigation": 1,
      "widget_id": 1
    }
  ],
  "settings": [
    {
      "appearance": {
        "APPEARANCE_AUTHOR_TEXT_COLOUR": "#FF000000",
        "APPEARANCE_AUTHOR_TEXT_HIDE": false,
        "APPEARANCE_AUTHOR_TEXT_SIZE": 16,
        "APPEARANCE_COLOUR": "#FFFF1493",
        "APPEARANCE_POSITION_TEXT_COLOUR": "#FF000000",
        "APPEARANCE_POSITION_TEXT_HIDE": false,
        "APPEARANCE_POSITION_TEXT_SIZE": 16,
        "APPEARANCE_TEXT_COLOUR": "#FF0000FF",
        "APPEARANCE_TEXT_FAMILY": "Monospace",
        "APPEARANCE_TEXT_FORCE_ITALIC_REGULAR": true,
        "APPEARANCE_TEXT_SIZE": 18,
        "APPEARANCE_TEXT_STYLE": "Bold",
        "APPEARANCE_TOOLBAR_COLOUR": "#FFFFFFFF",
        "APPEARANCE_TOOLBAR_FAVOURITE": true,
        "APPEARANCE_TOOLBAR_FIRST": true,
        "APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR": false,
        "APPEARANCE_TOOLBAR_PREVIOUS": false,
        "APPEARANCE_TOOLBAR_RANDOM": false,
        "APPEARANCE_TOOLBAR_SEQUENTIAL": true,
        "APPEARANCE_TOOLBAR_SHARE": false,
        "APPEARANCE_TRANSPARENCY": 40
      },
      "quotations": {
        "CONTENT_ADD_TO_PREVIOUS_ALL": true,
        "CONTENT_ALL": true,
        "CONTENT_AUTHOR": false,
        "CONTENT_AUTHOR_NAME": "a0",
        "CONTENT_FAVOURITES": false,
        "CONTENT_SEARCH": false,
        "CONTENT_SEARCH_COUNT": 0,
        "CONTENT_SEARCH_FAVOURITES_ONLY": false,
        "CONTENT_SEARCH_TEXT": "some search text",
        "DATABASE_EXTERNAL": false,
        "DATABASE_INTERNAL": true
      },
      "schedule": {
        "EVENT_DAILY": true,
        "EVENT_DAILY_HOUR": 7,
        "EVENT_DAILY_MINUTE": 1,
        "EVENT_DEVICE_UNLOCK": true,
        "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION": true,
        "EVENT_DISPLAY_WIDGET": false,
        "EVENT_NEXT_RANDOM": false,
        "EVENT_NEXT_SEQUENTIAL": true
      },
      "widget_id": 1
    }
  ]
}
            */

            assertTrue(backupTransfer.current.size == 2)
            assertTrue(backupTransfer.favourites.size == 2)
            assertTrue(backupTransfer.previous.size == 2)
        }
    }

    @Test
    fun restoreWhereExternalNotBeenPopulated() {
        if (canWorkWithMockk()) {
            // set up
            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(2)

            createSharedPreferencesWithLocalCode(getLocalCode())

            insertInternalQuotations()

            // restore
            val restoreTransfer = getTransferAsset("restore_one_widget_with_external.json")
            TransferRestore().restore(context, databaseRepositoryDouble, restoreTransfer)

            // assert
            val backupTransfer = TransferBackup(context).transfer(databaseRepositoryDouble)
            val restoreJson = TransferRestore().asJson(restoreTransfer)

//            val backupJson = TransferBackup(context).asJson(backupTransfer)
            /*
{
  "code": "F9aT8HEW6d",
  "current": [
    {
      "db": "internal",
      "digest": "d1234567",
      "widget_id": 2
    }
  ],
  "favourite": [
    {
      "db": "internal",
      "digest": "d5678901",
      "navigation": 2
    }
  ],
  "previous": [
    {
      "content_type": 1,
      "db": "internal",
      "digest": "d1234567",
      "navigation": 2,
      "widget_id": 2
    }
  ],
  "settings": [
    {
      "appearance": {
        "APPEARANCE_AUTHOR_TEXT_COLOUR": "#FF000000",
        "APPEARANCE_AUTHOR_TEXT_HIDE": false,
        "APPEARANCE_AUTHOR_TEXT_SIZE": 16,
        "APPEARANCE_COLOUR": "#FFFF1493",
        "APPEARANCE_POSITION_TEXT_COLOUR": "#FF000000",
        "APPEARANCE_POSITION_TEXT_HIDE": false,
        "APPEARANCE_POSITION_TEXT_SIZE": 16,
        "APPEARANCE_TEXT_COLOUR": "#FF0000FF",
        "APPEARANCE_TEXT_FAMILY": "Monospace",
        "APPEARANCE_TEXT_FORCE_ITALIC_REGULAR": true,
        "APPEARANCE_TEXT_SIZE": 18,
        "APPEARANCE_TEXT_STYLE": "Bold",
        "APPEARANCE_TOOLBAR_COLOUR": "#FFFFFFFF",
        "APPEARANCE_TOOLBAR_FAVOURITE": true,
        "APPEARANCE_TOOLBAR_FIRST": true,
        "APPEARANCE_REMOVE_SPACE_ABOVE_TOOLBAR": false,
        "APPEARANCE_TOOLBAR_PREVIOUS": false,
        "APPEARANCE_TOOLBAR_RANDOM": false,
        "APPEARANCE_TOOLBAR_SEQUENTIAL": true,
        "APPEARANCE_TOOLBAR_SHARE": false,
        "APPEARANCE_TRANSPARENCY": 40
      },
      "quotations": {
        "CONTENT_ADD_TO_PREVIOUS_ALL": true,
        "CONTENT_ALL": true,
        "CONTENT_AUTHOR": false,
        "CONTENT_AUTHOR_NAME": "a0",
        "CONTENT_FAVOURITES": false,
        "CONTENT_SEARCH": false,
        "CONTENT_SEARCH_COUNT": 0,
        "CONTENT_SEARCH_FAVOURITES_ONLY": false,
        "CONTENT_SEARCH_TEXT": "some search text",
        "DATABASE_EXTERNAL": false,
        "DATABASE_INTERNAL": true
      },
      "schedule": {
        "EVENT_DAILY": true,
        "EVENT_DAILY_HOUR": 7,
        "EVENT_DAILY_MINUTE": 1,
        "EVENT_DEVICE_UNLOCK": true,
        "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION": true,
        "EVENT_DISPLAY_WIDGET": false,
        "EVENT_NEXT_RANDOM": false,
        "EVENT_NEXT_SEQUENTIAL": true
      },
      "widget_id": 2
    }
  ]
}
            */

            assertTrue(backupTransfer.current.size == 1)
            assertTrue(backupTransfer.favourites.size == 1)
            assertTrue(backupTransfer.previous.size == 1)
        }
    }
}
