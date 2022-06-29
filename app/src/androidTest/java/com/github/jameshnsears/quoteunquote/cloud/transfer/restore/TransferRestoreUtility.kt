package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockkObject
import java.io.BufferedReader
import java.io.InputStream

open class TransferRestoreUtility : QuoteUnquoteModelUtility() {
    protected fun createSharedPreferencesWithLocalCode(localCode: String) {
        val preferencesFacade = PreferencesFacade(0, context)
        preferencesFacade.preferenceHelper?.setPreference(
            "0:CONTENT_FAVOURITES_LOCAL_CODE",
            localCode
        )
    }

    protected fun getTransferAsset(assetFilename: String): Transfer {
        val inputStream: InputStream =
            InstrumentationRegistry.getInstrumentation().context.resources.assets
                .open(
                    assetFilename
                )

        return Gson().fromJson(
            inputStream.bufferedReader().use(BufferedReader::readText),
            Transfer::class.java
        )
    }

    protected fun getLocalCode(): String {
        return "F9aT8HEW6d"
    }

    protected fun setupWidgets(widgetIds: IntArray) {
        createSharedPreferencesWithLocalCode(getLocalCode())
        insertQuotationTestData01()
        insertQuotationTestData02()
        insertQuotationTestData03()

        for (widgetId in widgetIds) {
            setDefaultQuotationAll(widgetId)
        }

        mockkObject(TransferUtility)
        every { TransferUtility.getWidgetIds(context) } returns widgetIds
    }
}
