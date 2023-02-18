package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.cloud.BuildConfig
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConfigTest {
    @Test
    fun ensureSyncEndpointsAvailable() {
        assertTrue(BuildConfig.REMOTE_DEVICE_ENDPOINT.indexOf("https://us-central1-") == 0)
    }
}
