package com.github.jameshnsears.quoteunquote

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class QuoteUnquoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val seedColor = 0xFF6750A4.toInt()
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions
                .Builder()
                .setContentBasedSource(seedColor)
                .build(),
        )
    }
}
