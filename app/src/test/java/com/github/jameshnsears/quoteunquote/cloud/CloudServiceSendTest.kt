package com.github.jameshnsears.quoteunquote.cloud

import android.content.Intent
import android.os.Build
import android.os.Handler
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CloudServiceSendTest : ShadowLoggingHelper() {
    @Test
    fun startService() {
        assertFalse(CloudServiceSend.isRunning)

        val cloudServiceSendIntent = Intent(getApplicationContext(), CloudServiceSendDouble::class.java)
        cloudServiceSendIntent.putExtra("savePayload", "{\"code\":\"bc5yX41a20\",\"digests\":[]}")
        cloudServiceSendIntent.putExtra("localCodeValue", "0123456789")

        val cloudServiceSend = Robolectric.buildService(CloudServiceSendDouble::class.java).get()

        cloudServiceSend.onStartCommand(cloudServiceSendIntent, 0, 0)
        assertTrue(CloudServiceSend.isRunning)

        Thread.sleep(250)
        assertFalse(CloudServiceSend.isRunning)

        cloudServiceSend.onDestroy()
    }

    @Test
    fun noNetwork() {
        CloudServiceHelper.showNoNetworkToast(getApplicationContext(), Handler())
    }
}
