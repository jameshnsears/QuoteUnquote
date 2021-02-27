package com.github.jameshnsears.quoteunquote.configure

import android.content.Context
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade

class ConfigurePreferences(widgetId: Int, applicationContext: Context): PreferencesFacade(widgetId, applicationContext) {
    private val CONFIGURE_SCROLL_Y = "CONFIGURE_SCROLL_Y"

    fun getScrollY(): Int? {
        return preferenceHelper?.getPreferenceInt(getPreferenceKey(CONFIGURE_SCROLL_Y))
    }

    fun setScrollY(value: Int) {
        preferenceHelper?.setPreference(getPreferenceKey(CONFIGURE_SCROLL_Y), value)
    }
}