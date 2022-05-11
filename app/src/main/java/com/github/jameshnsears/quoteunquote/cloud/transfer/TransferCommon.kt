package com.github.jameshnsears.quoteunquote.cloud.transfer

import com.google.gson.GsonBuilder

open class TransferCommon {
    fun asJson(transfer: Transfer): String {
        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        return builder.create().toJson(transfer)
    }
}
