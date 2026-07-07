package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.cloud.BuildConfig
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BuildConfigTest {
    @Test
    fun ensureSyncEndpointsAvailable() {
        assertThat(BuildConfig.REMOTE_DEVICE_ENDPOINT.indexOf("https://us-central1-") == 0, `is`(true))
    }
}
