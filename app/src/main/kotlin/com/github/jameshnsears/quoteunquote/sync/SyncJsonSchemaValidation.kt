package com.github.jameshnsears.quoteunquote.sync

import android.content.Context
import net.pwall.json.schema.JSONSchema
import timber.log.Timber
import java.io.BufferedReader

class SyncJsonSchemaValidation {
    // https://github.com/pwall567/json-kotlin-schema
    companion object {
        fun isJsonValid(context: Context, json: String): Boolean {
            val schema = JSONSchema.parse(getSchema(context))

            if (!schema.validate(json)) {
                val output = schema.validateBasic(json)
                output.errors?.forEach {
                    Timber.e("${it.error} - ${it.instanceLocation}")
                }

                return false
            }

            return true
        }

        private fun getSchema(context: Context): String {
            val inputStream =
                context.resources.assets
                    .open(
                        "sync.scheme.json",
                    )
            return inputStream.bufferedReader().use(BufferedReader::readText)
        }
    }
}
