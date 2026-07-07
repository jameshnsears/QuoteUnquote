package com.github.jameshnsears.quoteunquote.cloud

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class CloudServiceBackupTest : ShadowLoggingHelper() {
    @Test
    fun startService() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = Intent(context, CloudServiceBackup::class.java)

        val serviceController = Robolectric.buildService(CloudServiceBackup::class.java, intent)
        serviceController.create().startCommand(0, 0)

        assertThat(CloudService.isRunning(), `is`(true))

        CloudService.stopRunning()
    }
}
