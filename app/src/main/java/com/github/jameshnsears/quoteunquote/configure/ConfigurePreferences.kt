package com.github.jameshnsears.quoteunquote.configure

import android.content.Context
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade

class ConfigurePreferences(widgetId: Int, applicationContext: Context) :
    PreferencesFacade(widgetId, applicationContext) {

    private val activeFragment = "CONFIGURE_ACTIVE_FRAGMENT"

    fun getActiveFragment(): String? {
        return preferenceHelper?.getPreferenceString(getPreferenceKey(activeFragment))
    }

    fun setActiveFragment(value: String) {
        preferenceHelper?.setPreference(getPreferenceKey(activeFragment), value)
    }
}
