package com.github.jameshnsears.quoteunquote.configure

import android.content.Context
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade

class ConfigurePreferences(widgetId: Int, applicationContext: Context) :
    PreferencesFacade(widgetId, applicationContext) {

    private val configureScrollY = "CONFIGURE_SCROLLY"

    fun getScrollY(): Int? {
        return preferenceHelper?.getPreferenceInt(getPreferenceKey(configureScrollY))
    }

    fun setScrollY(value: Int) {
        preferenceHelper?.setPreference(getPreferenceKey(configureScrollY), value)
    }
}
