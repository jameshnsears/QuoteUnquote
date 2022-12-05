package com.github.jameshnsears.quoteunquote.database

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DatabaseQuotationEntityTest : ShadowLoggingHelper() {
    @Test
    fun shareQuotationAndAuthor() {
        val quotationEntity = QuotationEntity(
            "digest",
            "wikipedia",
            "author",
            "quotation"
        )

        assertEquals("quotation\n\nauthor", quotationEntity.shareQuotationAuthor())
    }

    @Test
    fun shareQuotation() {
        val quotationEntity = QuotationEntity(
            "digest",
            "wikipedia",
            "author",
            "quotation"
        )

        assertEquals("quotation", quotationEntity.shareQuotation())
    }
}
