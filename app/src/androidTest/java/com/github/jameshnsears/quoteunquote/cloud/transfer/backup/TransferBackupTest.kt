package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import android.os.Build
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.Assert
import org.junit.Test

class TransferBackupTest : DatabaseTestHelper() {
    /*
    {
      "code": "012345672e",
      "settings": [
        {
          "quotations": {
            "CONTENT_ADD_TO_PREVIOUS_ALL": true,
            "CONTENT_ALL": false,
            "CONTENT_AUTHOR": false,
            "CONTENT_AUTHOR_NAME": "",
            "CONTENT_FAVOURITES": false,
            "CONTENT_SEARCH": false,
            "CONTENT_SEARCH_COUNT": -1,
            "CONTENT_SEARCH_TEXT": ""
          },
          "appearance": {
            "APPEARANCE_TRANSPARENCY": -1,
            "APPEARANCE_COLOUR": "#FFF8FD89",
            "APPEARANCE_TEXT_FAMILY": "Sans Serif",
            "APPEARANCE_TEXT_STYLE": "Regular",
            "APPEARANCE_TEXT_SIZE": 16,
            "APPEARANCE_TEXT_COLOUR": "#FF000000",
            "APPEARANCE_TOOLBAR_COLOUR": "",
            "APPEARANCE_TOOLBAR_FIRST": false,
            "APPEARANCE_TOOLBAR_PREVIOUS": true,
            "APPEARANCE_TOOLBAR_FAVOURITE": true,
            "APPEARANCE_TOOLBAR_SHARE": true,
            "APPEARANCE_TOOLBAR_RANDOM": true,
            "APPEARANCE_TOOLBAR_SEQUENTIAL": false
          },
          "schedule": {
            "EVENT_NEXT_RANDOM": true,
            "EVENT_NEXT_SEQUENTIAL": false,
            "EVENT_DISPLAY_WIDGET": true,
            "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION": false,
            "EVENT_DAILY": false,
            "EVENT_DEVICE_UNLOCK": false,
            "EVENT_DAILY_MINUTE": -1,
            "EVENT_DAILY_HOUR": -1
          },
          "widget_id": 12
        },
        {
          "quotations": {
            "CONTENT_ADD_TO_PREVIOUS_ALL": true,
            "CONTENT_ALL": false,
            "CONTENT_AUTHOR": false,
            "CONTENT_AUTHOR_NAME": "",
            "CONTENT_FAVOURITES": false,
            "CONTENT_SEARCH": false,
            "CONTENT_SEARCH_COUNT": -1,
            "CONTENT_SEARCH_TEXT": ""
          },
          "appearance": {
            "APPEARANCE_TRANSPARENCY": -1,
            "APPEARANCE_COLOUR": "#FFF8FD89",
            "APPEARANCE_TEXT_FAMILY": "Sans Serif",
            "APPEARANCE_TEXT_STYLE": "Regular",
            "APPEARANCE_TEXT_SIZE": 16,
            "APPEARANCE_TEXT_COLOUR": "#FF000000",
            "APPEARANCE_TOOLBAR_COLOUR": "",
            "APPEARANCE_TOOLBAR_FIRST": false,
            "APPEARANCE_TOOLBAR_PREVIOUS": true,
            "APPEARANCE_TOOLBAR_FAVOURITE": true,
            "APPEARANCE_TOOLBAR_SHARE": true,
            "APPEARANCE_TOOLBAR_RANDOM": true,
            "APPEARANCE_TOOLBAR_SEQUENTIAL": false
          },
          "schedule": {
            "EVENT_NEXT_RANDOM": true,
            "EVENT_NEXT_SEQUENTIAL": false,
            "EVENT_DISPLAY_WIDGET": true,
            "EVENT_DISPLAY_WIDGET_AND_NOTIFICATION": false,
            "EVENT_DAILY": false,
            "EVENT_DEVICE_UNLOCK": false,
            "EVENT_DAILY_MINUTE": -1,
            "EVENT_DAILY_HOUR": -1
          },
          "widget_id": 13
        }
      ],
      "current": [
        {
          "digest": "7a36e553",
          "widget_id": 12
        },
        {
          "digest": "7a36e553",
          "widget_id": 13
        }
      ],
      "previous": [
        {
          "content_type": 4,
          "digest": "d3456789",
          "navigation": 6,
          "widget_id": 13
        },
        {
          "content_type": 3,
          "digest": "d1234567",
          "navigation": 5,
          "widget_id": 12
        },
        {
          "content_type": 2,
          "digest": "d2345678",
          "navigation": 4,
          "widget_id": 13
        },
        {
          "content_type": 1,
          "digest": "d4567890",
          "navigation": 3,
          "widget_id": 12
        },
        {
          "content_type": 1,
          "digest": "7a36e553",
          "navigation": 2,
          "widget_id": 13
        },
        {
          "content_type": 1,
          "digest": "7a36e553",
          "navigation": 1,
          "widget_id": 12
        }
      ],
      "favourite": [
        {
          "digest": "d4567890",
          "navigation": 2
        },
        {
          "digest": "d2345678",
          "navigation": 1
        }
      ]
    }
    */

    @Test
    fun transfer() {
        val transferCurrentTest = TransferBackupCurrentTest()
        val transferPreviousTest = TransferBackupPreviousTest()
        val transferFavouriteTest = TransferBackupFavouriteTest()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            insertQuotationTestData01()
            insertQuotationTestData02()

            val backupTransfer = spyk(TransferBackup(context))
            val transferCode = "012345672e"
            every { backupTransfer.getLocalCode() } returns transferCode

            mockkObject(TransferUtility)
            every { TransferUtility.getWidgetIds(context) } returns intArrayOf(12, 13)

            transferCurrentTest.setupTestData()
            transferPreviousTest.setupTestData()
            transferFavouriteTest.setupTestData()

            val transfer = backupTransfer.transfer(databaseRepositoryDouble)
            Assert.assertEquals(transfer.code, transferCode)

            Assert.assertNotNull(transfer.settings)
            Assert.assertEquals(transfer.settings.size, 2)
            Assert.assertNotNull(transfer.current)
            Assert.assertNotNull(transfer.previous)
            Assert.assertNotNull(transfer.favourites)
        }
    }
}
