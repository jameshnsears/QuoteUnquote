package com.github.jameshnsears.quoteunquote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper

abstract class QuoteUnquoteModelUtility : DatabaseTestHelper() {
    @JvmField
    var quoteUnquoteModelDouble = QuoteUnquoteModelDouble()
    @JvmField
    val context : Context = ApplicationProvider.getApplicationContext()
}
