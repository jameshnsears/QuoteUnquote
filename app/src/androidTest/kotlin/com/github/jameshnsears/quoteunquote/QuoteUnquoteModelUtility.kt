package com.github.jameshnsears.quoteunquote

import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper

abstract class QuoteUnquoteModelUtility : DatabaseTestHelper() {
    @JvmField
    var quoteUnquoteModelDouble = QuoteUnquoteModelDouble()

    fun getImportAsset(filename: String) =
        InstrumentationRegistry.getInstrumentation().context.resources.assets
            .open(
                filename,
            )
}
