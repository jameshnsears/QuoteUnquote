package com.github.jameshnsears.quoteunquote.utils.logging

import org.junit.Before
import org.robolectric.shadows.ShadowLog

open class ShadowLoggingHelper {
    @Before
    fun logging() {
        ShadowLog.stream = System.out
    }
}
