package com.github.jameshnsears.quoteunquote.cloud.transfer

import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder

open class GsonTestHelper : DatabaseTestHelper() {
    fun gson(): Gson {
        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        return builder.create()
    }
}
