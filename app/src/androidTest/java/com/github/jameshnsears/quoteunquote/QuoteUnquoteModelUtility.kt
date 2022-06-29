package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper

abstract class QuoteUnquoteModelUtility : DatabaseTestHelper() {
    @JvmField
    var quoteUnquoteModelDouble = QuoteUnquoteModelDouble()
}
