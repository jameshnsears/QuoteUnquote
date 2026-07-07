package com.github.jameshnsears.quoteunquote.db

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class DatabaseQuotationEntityTest : ShadowLoggingHelper() {
    @Test
    fun shareQuotationAndAuthor() {
        val quotationEntity =
            QuotationEntity(
                "digest",
                "wikipedia",
                "author",
                "quotation",
            )

        assertThat(quotationEntity.shareQuotationAuthor(), equalTo("quotation\n\nauthor"))
    }

    @Test
    fun shareQuotation() {
        val quotationEntity =
            QuotationEntity(
                "digest",
                "wikipedia",
                "author",
                "quotation",
            )

        assertThat(quotationEntity.shareQuotation(), equalTo("quotation"))
    }
}
